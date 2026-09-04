package org.example.merchant_ai_operation.publicapi.promotion.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//活动项目返回VO
public record PublicPromotionActivityItemVO(
        Long activityId,
        Long activityItemId,
        String name,
        String productName,
        String skuName,
        BigDecimal activityPrice,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String status,
        String stockStatus,
        Integer limitPerUser
) {
}
