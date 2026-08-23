package org.example.merchant_ai_operation.order;

import org.example.merchant_ai_operation.order.entity.CheckoutGroup;
import org.example.merchant_ai_operation.order.mapper.CheckoutGroupMapper;
import org.example.merchant_ai_operation.order.service.CheckoutGroupService;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.merchant_ai_operation.common.BizException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutGroupServiceTest {

    @Mock
    private CheckoutGroupMapper checkoutGroupMapper;

    private CheckoutGroupService checkoutGroupService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-23T04:00:00Z"),
                ZoneId.of("Asia/Shanghai")
        );

        checkoutGroupService = new CheckoutGroupService(
                checkoutGroupMapper,
                clock
        );

        LoginPrincipal principal =
                new LoginPrincipal(5001L, null, "CONSUMER");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPendingShouldBuildConsumerCheckoutGroup() {
        when(checkoutGroupMapper.insert(any(CheckoutGroup.class)))
                .thenAnswer(invocation -> {
                    CheckoutGroup group = invocation.getArgument(0);
                    group.setId(1L);
                    return 1;
                });

        CheckoutGroup result =
                checkoutGroupService.createPending(new BigDecimal("199.00"));

        assertEquals(1L, result.getId());
        assertEquals(5001L, result.getConsumerId());
        assertEquals("PENDING_PAYMENT", result.getStatus());
        assertEquals(new BigDecimal("199.00"), result.getTotalAmount());
        assertTrue(result.getCheckoutNo().startsWith("CHK20260823120000"));

        ArgumentCaptor<CheckoutGroup> captor =
                ArgumentCaptor.forClass(CheckoutGroup.class);

        verify(checkoutGroupMapper).insert(captor.capture());
        assertEquals(5001L, captor.getValue().getConsumerId());
    }

    @Test
    void getMineShouldQueryWithCurrentConsumerId() {
        CheckoutGroup group = new CheckoutGroup();
        group.setId(7L);
        group.setConsumerId(5001L);

        when(checkoutGroupMapper.selectByIdAndConsumerId(7L, 5001L))
                .thenReturn(group);

        CheckoutGroup result = checkoutGroupService.getMine(7L);

        assertEquals(7L, result.getId());
        verify(checkoutGroupMapper)
                .selectByIdAndConsumerId(7L, 5001L);
    }

    @Test
    void createPendingShouldRejectNegativeAmount() {
        assertThrows(
                BizException.class,
                () -> checkoutGroupService.createPending(new BigDecimal("-1.00"))
        );

        verifyNoInteractions(checkoutGroupMapper);
    }

    @Test
    void getMineShouldRejectMissingCheckoutGroup() {
        when(checkoutGroupMapper.selectByIdAndConsumerId(99L, 5001L))
                .thenReturn(null);

        assertThrows(
                BizException.class,
                () -> checkoutGroupService.getMine(99L)
        );
    }
}