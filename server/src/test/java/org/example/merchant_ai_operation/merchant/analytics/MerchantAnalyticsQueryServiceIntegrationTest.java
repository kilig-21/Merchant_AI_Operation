package org.example.merchant_ai_operation.merchant.analytics;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.analytics.service.MerchantAnalyticsQueryService;
import org.example.merchant_ai_operation.merchant.analytics.vo.AfterSaleRateVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.MerchantOperatingSummaryVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.LowStockSkuVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.OrderStatusStatisticsVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.PromotionPerformanceVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.TopProductVO;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
@Sql(
        scripts = "/a1-analytics-fixture.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(
        scripts = "/a1-analytics-cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS
)
class MerchantAnalyticsQueryServiceIntegrationTest {

    private static final long TENANT_A = 9200000000001L;
    private static final long TENANT_B = 9200000000002L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 8, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 8, 3);

    @Autowired
    private MerchantAnalyticsQueryService analyticsQueryService;

    @BeforeEach
    void loginAsTenantA() {
        loginAsMerchant(TENANT_A);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnFixedOperatingSummaryForTenantA() {
        MerchantOperatingSummaryVO summary =
                analyticsQueryService.getOperatingSummary(START_DATE, END_DATE);

        assertEquals(3L, summary.validOrderCount());
        assertEquals(2L, summary.paidOrderCount());
        assertMoney("350.00", summary.paidRevenue());
        assertMoney("175.00", summary.averageOrderValue());
        assertEquals(1L, summary.pendingPaymentCount());
        assertEquals(1L, summary.lowStockProductCount());
    }

    @Test
    void shouldReturnTopProductsInStableOrder() {
        List<TopProductVO> products =
                analyticsQueryService.getTopProducts(START_DATE, END_DATE, 10);

        assertEquals(2, products.size());

        TopProductVO first = products.getFirst();
        assertEquals(9200000000201L, first.skuId());
        assertEquals("A1 headphones standard", first.skuName());
        assertEquals(5L, first.soldQuantity());
        assertMoney("250.00", first.paidRevenue());

        TopProductVO second = products.get(1);
        assertEquals(9200000000202L, second.skuId());
        assertEquals(1L, second.soldQuantity());
        assertMoney("100.00", second.paidRevenue());
    }

    @Test
    void shouldReturnLowStockSkuSnapshotForCurrentTenant() {
        List<LowStockSkuVO> lowStockSkus = analyticsQueryService.getLowStockSkus(10);

        assertEquals(1, lowStockSkus.size());
        LowStockSkuVO sku = lowStockSkus.getFirst();
        assertEquals(9200000000201L, sku.skuId());
        assertEquals(9200000000101L, sku.spuId());
        assertEquals("A1 test headphones", sku.productName());
        assertEquals("A1 headphones standard", sku.skuName());
        assertMoney("50.00", sku.salePrice());
        assertEquals(4, sku.availableStock());
        assertEquals(0, sku.lockedStock());
    }

    @Test
    void shouldReturnOrderStatusStatisticsForTenantA() {
        OrderStatusStatisticsVO statistics =
                analyticsQueryService.getOrderStatusStatistics(START_DATE, END_DATE);

        assertEquals(5L, statistics.totalOrderCount());
        assertEquals(1L, statistics.pendingPaymentOrderCount());
        assertEquals(2L, statistics.paidOrderCount());
        assertEquals(1L, statistics.cancelledOrderCount());
        assertEquals(1L, statistics.closedOrderCount());
    }

    @Test
    void shouldReturnFixedPromotionPerformanceForTenantA() {
        List<PromotionPerformanceVO> promotions =
                analyticsQueryService.getPromotionPerformance(START_DATE, END_DATE, 5);

        assertEquals(1, promotions.size());
        PromotionPerformanceVO promotion = promotions.getFirst();
        assertEquals(9200000000501L, promotion.activityId());
        assertEquals(4L, promotion.reservationCount());
        assertEquals(2L, promotion.orderCreatedCount());
        assertEquals(3L, promotion.successfulQuantity());
        assertMoney("90.00", promotion.promotionRevenue());
        assertMoney("0.50", promotion.orderConversionRate());
    }

    @Test
    void shouldReturnFixedAfterSaleRateForTenantA() {
        AfterSaleRateVO afterSaleRate =
                analyticsQueryService.getAfterSaleRate(START_DATE, END_DATE);

        assertEquals(3L, afterSaleRate.paidOrderItemCount());
        assertEquals(1L, afterSaleRate.afterSaleOrderItemCount());
        assertMoney("0.33", afterSaleRate.afterSaleRate());
    }

    @Test
    void shouldKeepTenantBDataOutOfTenantAResults() {
        loginAsMerchant(TENANT_B);

        MerchantOperatingSummaryVO summary =
                analyticsQueryService.getOperatingSummary(START_DATE, END_DATE);

        assertEquals(1L, summary.validOrderCount());
        assertEquals(1L, summary.paidOrderCount());
        assertMoney("999.00", summary.paidRevenue());
        assertMoney("999.00", summary.averageOrderValue());
        assertEquals(0L, summary.pendingPaymentCount());
        assertEquals(1L, summary.lowStockProductCount());

        OrderStatusStatisticsVO statistics =
                analyticsQueryService.getOrderStatusStatistics(START_DATE, END_DATE);
        assertEquals(1L, statistics.totalOrderCount());
        assertEquals(0L, statistics.pendingPaymentOrderCount());
        assertEquals(1L, statistics.paidOrderCount());
        assertEquals(0L, statistics.cancelledOrderCount());
        assertEquals(0L, statistics.closedOrderCount());
    }

    @Test
    void shouldReturnZeroAndEmptyResultsForRangeWithoutData() {
        LocalDate emptyStart = LocalDate.of(2026, 8, 10);
        LocalDate emptyEnd = LocalDate.of(2026, 8, 12);

        MerchantOperatingSummaryVO summary =
                analyticsQueryService.getOperatingSummary(emptyStart, emptyEnd);

        assertEquals(0L, summary.validOrderCount());
        assertEquals(0L, summary.paidOrderCount());
        assertMoney("0.00", summary.paidRevenue());
        assertMoney("0.00", summary.averageOrderValue());
        assertEquals(0L, summary.pendingPaymentCount());
        assertEquals(1L, summary.lowStockProductCount());
        assertEquals(List.of(), analyticsQueryService.getTopProducts(emptyStart, emptyEnd, 5));
        assertEquals(1, analyticsQueryService.getLowStockSkus(5).size());
        assertEquals(List.of(), analyticsQueryService.getPromotionPerformance(emptyStart, emptyEnd, 5));

        AfterSaleRateVO afterSaleRate = analyticsQueryService.getAfterSaleRate(emptyStart, emptyEnd);
        assertEquals(0L, afterSaleRate.paidOrderItemCount());
        assertEquals(0L, afterSaleRate.afterSaleOrderItemCount());
        assertMoney("0.00", afterSaleRate.afterSaleRate());

        OrderStatusStatisticsVO statistics =
                analyticsQueryService.getOrderStatusStatistics(emptyStart, emptyEnd);
        assertEquals(0L, statistics.totalOrderCount());
        assertEquals(0L, statistics.pendingPaymentOrderCount());
        assertEquals(0L, statistics.paidOrderCount());
        assertEquals(0L, statistics.cancelledOrderCount());
        assertEquals(0L, statistics.closedOrderCount());
    }

    @Test
    void shouldRejectInvalidDateRangeAndListLimit() {
        BizException reversed = assertThrows(
                BizException.class,
                () -> analyticsQueryService.getOperatingSummary(END_DATE, START_DATE)
        );
        assertEquals(400, reversed.getCode());

        BizException tooLong = assertThrows(
                BizException.class,
                () -> analyticsQueryService.getOperatingSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 9, 1)
                )
        );
        assertEquals(400, tooLong.getCode());

        assertThrows(
                BizException.class,
                () -> analyticsQueryService.getTopProducts(START_DATE, END_DATE, 0)
        );
        assertThrows(
                BizException.class,
                () -> analyticsQueryService.getPromotionPerformance(START_DATE, END_DATE, 11)
        );
        assertThrows(
                BizException.class,
                () -> analyticsQueryService.getLowStockSkus(0)
        );
    }

    private void loginAsMerchant(long tenantId) {
        LoginPrincipal principal = new LoginPrincipal(9200000000801L, tenantId, "MERCHANT_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
