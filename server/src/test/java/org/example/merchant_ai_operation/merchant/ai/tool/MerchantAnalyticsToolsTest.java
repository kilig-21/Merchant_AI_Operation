package org.example.merchant_ai_operation.merchant.ai.tool;

import org.example.merchant_ai_operation.merchant.analytics.service.MerchantAnalyticsQueryService;
import org.example.merchant_ai_operation.merchant.analytics.vo.AfterSaleRateVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.LowStockSkuVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.OrderStatusStatisticsVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.MerchantOperatingSummaryVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.PromotionPerformanceVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.TopProductVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantAnalyticsToolsTest {

    @Test
    void delegatesOperatingSummaryQueryToAnalyticsService() {
        MerchantAnalyticsQueryService analyticsQueryService =
                mock(MerchantAnalyticsQueryService.class);

        AiToolUsageTracker toolUsageTracker = new AiToolUsageTracker();

        MerchantAnalyticsTools tools =
                new MerchantAnalyticsTools(
                        analyticsQueryService,
                        toolUsageTracker
                );

        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);

        MerchantOperatingSummaryVO expected =
                new MerchantOperatingSummaryVO(
                        3L,
                        2L,
                        new BigDecimal("350.00"),
                        new BigDecimal("175.00"),
                        1L,
                        1L
                );

        when(analyticsQueryService.getOperatingSummary(startDate, endDate))
                .thenReturn(expected);

        try (AiToolUsageTracker.Scope scope = toolUsageTracker.openScope()) {
            MerchantOperatingSummaryVO actual =
                    tools.getOperatingSummary(startDate, endDate);

            assertSame(expected, actual);
            assertTrue(scope.businessDataUsed());
        }

        verify(analyticsQueryService)
                .getOperatingSummary(startDate, endDate);
    }

    @Test
    void delegatesTopProductsQueryToAnalyticsService() {
        MerchantAnalyticsQueryService analyticsQueryService =
                mock(MerchantAnalyticsQueryService.class);
        AiToolUsageTracker toolUsageTracker = new AiToolUsageTracker();
        MerchantAnalyticsTools tools = new MerchantAnalyticsTools(
                analyticsQueryService,
                toolUsageTracker
        );

        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);
        List<TopProductVO> expected = List.of(
                new TopProductVO(
                        101L,
                        "轻量防晒衣",
                        8L,
                        new BigDecimal("800.00")
                )
        );

        when(analyticsQueryService.getTopProducts(startDate, endDate, 3))
                .thenReturn(expected);

        try (AiToolUsageTracker.Scope scope = toolUsageTracker.openScope()) {
            List<TopProductVO> actual = tools.getTopProducts(
                    startDate,
                    endDate,
                    3
            );

            assertSame(expected, actual);
            assertTrue(scope.businessDataUsed());
        }

        verify(analyticsQueryService).getTopProducts(startDate, endDate, 3);
    }

    @Test
    void delegatesPromotionPerformanceQueryToAnalyticsService() {
        MerchantAnalyticsQueryService analyticsQueryService =
                mock(MerchantAnalyticsQueryService.class);
        AiToolUsageTracker toolUsageTracker = new AiToolUsageTracker();
        MerchantAnalyticsTools tools = new MerchantAnalyticsTools(
                analyticsQueryService,
                toolUsageTracker
        );

        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);
        List<PromotionPerformanceVO> expected = List.of(
                new PromotionPerformanceVO(
                        301L,
                        "秋季上新",
                        50L,
                        12L,
                        10L,
                        new BigDecimal("1680.00"),
                        new BigDecimal("0.2400")
                )
        );

        when(analyticsQueryService.getPromotionPerformance(startDate, endDate, 3))
                .thenReturn(expected);

        try (AiToolUsageTracker.Scope scope = toolUsageTracker.openScope()) {
            List<PromotionPerformanceVO> actual =
                    tools.getPromotionPerformance(startDate, endDate, 3);

            assertSame(expected, actual);
            assertTrue(scope.businessDataUsed());
        }

        verify(analyticsQueryService)
                .getPromotionPerformance(startDate, endDate, 3);
    }

    @Test
    void delegatesAfterSaleRateQueryToAnalyticsService() {
        MerchantAnalyticsQueryService analyticsQueryService =
                mock(MerchantAnalyticsQueryService.class);
        AiToolUsageTracker toolUsageTracker = new AiToolUsageTracker();
        MerchantAnalyticsTools tools = new MerchantAnalyticsTools(
                analyticsQueryService,
                toolUsageTracker
        );

        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);
        AfterSaleRateVO expected = new AfterSaleRateVO(
                10L,
                2L,
                new BigDecimal("0.2000")
        );

        when(analyticsQueryService.getAfterSaleRate(startDate, endDate))
                .thenReturn(expected);

        try (AiToolUsageTracker.Scope scope = toolUsageTracker.openScope()) {
            AfterSaleRateVO actual = tools.getAfterSaleRate(startDate, endDate);

            assertSame(expected, actual);
            assertTrue(scope.businessDataUsed());
        }

        verify(analyticsQueryService).getAfterSaleRate(startDate, endDate);
    }

    @Test
    void delegatesLowStockSkuQueryToAnalyticsService() {
        MerchantAnalyticsQueryService analyticsQueryService =
                mock(MerchantAnalyticsQueryService.class);
        AiToolUsageTracker toolUsageTracker = new AiToolUsageTracker();
        MerchantAnalyticsTools tools = new MerchantAnalyticsTools(
                analyticsQueryService,
                toolUsageTracker
        );
        List<LowStockSkuVO> expected = List.of(
                new LowStockSkuVO(
                        101L,
                        11L,
                        "轻量防晒衣",
                        "雾蓝色 / L",
                        new BigDecimal("199.00"),
                        3,
                        1
                )
        );

        when(analyticsQueryService.getLowStockSkus(3)).thenReturn(expected);

        try (AiToolUsageTracker.Scope scope = toolUsageTracker.openScope()) {
            List<LowStockSkuVO> actual = tools.getLowStockSkus(3);

            assertSame(expected, actual);
            assertTrue(scope.businessDataUsed());
        }

        verify(analyticsQueryService).getLowStockSkus(3);
    }

    @Test
    void delegatesOrderStatusStatisticsQueryToAnalyticsService() {
        MerchantAnalyticsQueryService analyticsQueryService =
                mock(MerchantAnalyticsQueryService.class);
        AiToolUsageTracker toolUsageTracker = new AiToolUsageTracker();
        MerchantAnalyticsTools tools = new MerchantAnalyticsTools(
                analyticsQueryService,
                toolUsageTracker
        );
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);
        OrderStatusStatisticsVO expected = new OrderStatusStatisticsVO(
                8L,
                2L,
                3L,
                1L,
                2L
        );

        when(analyticsQueryService.getOrderStatusStatistics(startDate, endDate))
                .thenReturn(expected);

        try (AiToolUsageTracker.Scope scope = toolUsageTracker.openScope()) {
            OrderStatusStatisticsVO actual =
                    tools.getOrderStatusStatistics(startDate, endDate);

            assertSame(expected, actual);
            assertTrue(scope.businessDataUsed());
        }

        verify(analyticsQueryService)
                .getOrderStatusStatistics(startDate, endDate);
    }
}
