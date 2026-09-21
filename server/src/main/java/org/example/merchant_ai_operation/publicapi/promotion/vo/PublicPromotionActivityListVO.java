package org.example.merchant_ai_operation.publicapi.promotion.vo;

import java.time.LocalDateTime;
import java.util.List;

public record PublicPromotionActivityListVO(
        LocalDateTime serverTime,
        List<PublicPromotionActivityItemVO> activities
) {
}