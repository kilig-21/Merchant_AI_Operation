package org.example.merchant_ai_operation.merchant.ai.tool;

import org.example.merchant_ai_operation.merchant.ai.exception.AiChatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiToolUsageTrackerTest {

    @Test
    void clearsBusinessDataUsageAfterScopeCloses() {
        AiToolUsageTracker tracker = new AiToolUsageTracker();

        try (AiToolUsageTracker.Scope scope = tracker.openScope()) {
            assertFalse(scope.businessDataUsed());

            tracker.markBusinessDataUsed();

            assertTrue(scope.businessDataUsed());
        }

        try (AiToolUsageTracker.Scope nextScope = tracker.openScope()) {
            assertFalse(nextScope.businessDataUsed());
        }
    }

    @Test
    void rejectsSecondToolCallInSameScope() {
        AiToolUsageTracker tracker = new AiToolUsageTracker();

        try (AiToolUsageTracker.Scope scope = tracker.openScope()) {
            tracker.markBusinessDataUsed();

            AiChatException exception = assertThrows(
                    AiChatException.class,
                    tracker::markBusinessDataUsed
            );

            assertEquals(400, exception.getCode());
            assertEquals("一次对话最多调用一次经营查询工具", exception.getMessage());
            assertTrue(scope.businessDataUsed());
        }
    }
}
