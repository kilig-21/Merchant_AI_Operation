package org.example.merchant_ai_operation.promotion.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MerchantPromotionActivityVO(
        Long activityId,
        Long activityItemId,
        String name,
        String productName,
        Long skuId,
        String skuName,
        BigDecimal activityPrice,
        Integer stockTotal,
        Integer stockAvailable,
        Integer limitPerUser,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String status
) {
}