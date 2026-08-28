package org.example.merchant_ai_operation.merchant.dashboard.vo;

import java.math.BigDecimal;

public record MerchantDashboardMetricsVO (
        Long validOrderCount,
        BigDecimal paidRevenue,
        Long pendingPaymentCount,
        Long lowStockProductCount
){
}
