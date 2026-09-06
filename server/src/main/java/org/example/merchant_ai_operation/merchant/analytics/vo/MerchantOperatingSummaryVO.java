package org.example.merchant_ai_operation.merchant.analytics.vo;

import java.math.BigDecimal;


//经营汇总 VO
public record MerchantOperatingSummaryVO (
        Long validOrderCount,
        Long paidOrderCount,
        BigDecimal paidRevenue,
        BigDecimal averageOrderValue,
        Long pendingPaymentCount,
        Long lowStockProductCount
){}
