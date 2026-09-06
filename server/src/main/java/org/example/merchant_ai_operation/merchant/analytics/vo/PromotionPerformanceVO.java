package org.example.merchant_ai_operation.merchant.analytics.vo;

import java.math.BigDecimal;

//促销表现 VO
public record PromotionPerformanceVO(
        Long activityId,
        String activityName,
        Long reservationCount,
        Long orderCreatedCount,
        Long successfulQuantity,
        BigDecimal promotionRevenue,
        BigDecimal orderConversionRate
) {}