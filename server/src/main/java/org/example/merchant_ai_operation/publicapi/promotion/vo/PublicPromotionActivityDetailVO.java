package org.example.merchant_ai_operation.publicapi.promotion.vo;

import java.time.LocalDateTime;

//活动详情VO
public record PublicPromotionActivityDetailVO(
        LocalDateTime serverTime,
        PublicPromotionActivityItemVO activity
) {
}