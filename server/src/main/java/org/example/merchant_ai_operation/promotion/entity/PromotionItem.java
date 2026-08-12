package org.example.merchant_ai_operation.promotion.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
//活动商品表
public class PromotionItem {
    private Long id;
    private Long activityId;
    private Long tenantId;
    private Long skuId;
    private BigDecimal activityPrice;
    private Integer stockTotal;
    private Integer stockAvailable;
    private Integer limitPerUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}