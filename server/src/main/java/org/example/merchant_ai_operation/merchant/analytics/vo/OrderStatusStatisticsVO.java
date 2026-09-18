package org.example.merchant_ai_operation.merchant.analytics.vo;

/**
 * 指定日期范围内，当前商家订单按当前状态统计的数量。
 */
public record OrderStatusStatisticsVO(
        Long totalOrderCount,
        Long pendingPaymentOrderCount,
        Long paidOrderCount,
        Long cancelledOrderCount,
        Long closedOrderCount
) {
}
