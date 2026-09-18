package org.example.merchant_ai_operation.merchant.analytics;

import org.example.merchant_ai_operation.merchant.analytics.mapper.MerchantAnalyticsMapper;
import org.example.merchant_ai_operation.merchant.analytics.service.MerchantAnalyticsQueryService;
import org.example.merchant_ai_operation.merchant.analytics.vo.LowStockSkuVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.OrderStatusStatisticsVO;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantAnalyticsQueryServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsLowStockSkusOnlyForCurrentMerchantTenant() {
        MerchantAnalyticsMapper analyticsMapper = mock(MerchantAnalyticsMapper.class);
        MerchantAnalyticsQueryService service =
                new MerchantAnalyticsQueryService(analyticsMapper);
        loginAsMerchant(101L);

        List<LowStockSkuVO> expected = List.of(
                new LowStockSkuVO(
                        201L,
                        301L,
                        "轻量防晒衣",
                        "雾蓝色 / L",
                        new BigDecimal("199.00"),
                        5,
                        2
                )
        );
        when(analyticsMapper.selectLowStockSkus(101L, 5, 3))
                .thenReturn(expected);

        List<LowStockSkuVO> actual = service.getLowStockSkus(3);

        assertSame(expected, actual);
        verify(analyticsMapper).selectLowStockSkus(101L, 5, 3);
    }

    @Test
    void returnsOrderStatusStatisticsOnlyForCurrentMerchantTenantAndDateRange() {
        MerchantAnalyticsMapper analyticsMapper = mock(MerchantAnalyticsMapper.class);
        MerchantAnalyticsQueryService service =
                new MerchantAnalyticsQueryService(analyticsMapper);
        loginAsMerchant(101L);
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);
        LocalDateTime startAt = startDate.atStartOfDay();
        LocalDateTime endAt = endDate.plusDays(1).atStartOfDay();
        MerchantAnalyticsMapper.OrderStatusStatisticsRow row =
                new MerchantAnalyticsMapper.OrderStatusStatisticsRow(8L, 2L, 3L, 1L, 2L);
        when(analyticsMapper.selectOrderStatusStatistics(101L, startAt, endAt))
                .thenReturn(row);

        OrderStatusStatisticsVO actual =
                service.getOrderStatusStatistics(startDate, endDate);

        assertEquals(8L, actual.totalOrderCount());
        assertEquals(2L, actual.pendingPaymentOrderCount());
        assertEquals(3L, actual.paidOrderCount());
        assertEquals(1L, actual.cancelledOrderCount());
        assertEquals(2L, actual.closedOrderCount());
        verify(analyticsMapper).selectOrderStatusStatistics(101L, startAt, endAt);
    }

    private void loginAsMerchant(long tenantId) {
        LoginPrincipal principal = new LoginPrincipal(1L, tenantId, "MERCHANT_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }
}
