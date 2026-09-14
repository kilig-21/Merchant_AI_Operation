package org.example.merchant_ai_operation.merchant.ai.guard;

import org.example.merchant_ai_operation.merchant.ai.exception.AiChatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiChatGuardTest {

    private AiChatGuard guard;

    @BeforeEach
    void setUp() {
        guard = new AiChatGuard(Clock.fixed(
                Instant.parse("2026-09-09T06:00:00Z"),
                ZoneOffset.UTC
        ));
    }

    @Test
    void rejectsEleventhRequestFromSameUserInOneMinute() {
        for (int i = 0; i < 10; i++) {
            try (AiChatGuard.Permit ignored = guard.acquire(1001L)) {
                // 前十次请求均应取得并释放许可证。
            }
        }

        AiChatException exception = assertThrows(
                AiChatException.class,
                () -> guard.acquire(1001L)
        );

        assertEquals(429, exception.getCode());
        assertEquals("AI 请求过于频繁，请一分钟后再试", exception.getMessage());
    }

    @Test
    void keepsRateLimitsIndependentBetweenUsers() {
        for (int i = 0; i < 10; i++) {
            try (AiChatGuard.Permit ignored = guard.acquire(1001L)) {
                // 用尽用户 1001 的窗口额度。
            }
        }

        assertDoesNotThrow(() -> {
            try (AiChatGuard.Permit ignored = guard.acquire(1002L)) {
                // 用户 1002 拥有独立额度。
            }
        });
    }

    @Test
    void rejectsFifthConcurrentRequestAndAllowsItAfterRelease() {
        AiChatGuard.Permit first = guard.acquire(1001L);
        AiChatGuard.Permit second = guard.acquire(1002L);
        AiChatGuard.Permit third = guard.acquire(1003L);
        AiChatGuard.Permit fourth = guard.acquire(1004L);

        try {
            AiChatException exception = assertThrows(
                    AiChatException.class,
                    () -> guard.acquire(1005L)
            );
            assertEquals(429, exception.getCode());
            assertEquals("当前 AI 请求较多，请稍后重试", exception.getMessage());

            first.close();
            assertDoesNotThrow(() -> {
                try (AiChatGuard.Permit ignored = guard.acquire(1005L)) {
                    // 释放一个名额后，新请求应能进入。
                }
            });
        } finally {
            first.close();
            second.close();
            third.close();
            fourth.close();
        }
    }
}
