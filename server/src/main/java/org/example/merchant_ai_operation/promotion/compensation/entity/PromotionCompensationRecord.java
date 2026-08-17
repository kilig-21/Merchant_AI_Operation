package org.example.merchant_ai_operation.promotion.compensation.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
//Entity
public class PromotionCompensationRecord {

    private Long id;
    private String reservationId;
    private Long activityItemId;
    private Long tenantId;
    private Long consumerId;
    private String compensationType;
    private Integer quantity;
    private Integer stockChange;
    private Integer userQuantityChange;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}