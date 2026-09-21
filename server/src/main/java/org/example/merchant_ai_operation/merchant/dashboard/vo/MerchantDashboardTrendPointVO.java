package org.example.merchant_ai_operation.merchant.dashboard.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MerchantDashboardTrendPointVO(
    LocalDate date,
    Long orderCount,
    BigDecimal paidRevenue
){}
