package org.example.merchant_ai_operation.promotion.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
//活动表
public class PromotionActivity {
    private Long id;
    private Long tenantId;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}