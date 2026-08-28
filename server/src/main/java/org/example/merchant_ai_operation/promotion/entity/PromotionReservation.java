package org.example.merchant_ai_operation.promotion.entity;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PromotionReservation{
    private Long id;
    private String reservationId;
    private Long activityId;
    private Long activityItemId;
    private Long tenantId;
    private Long consumerId;
    private String requestKey;
    private Integer quantity;
    private BigDecimal unitPriceSnapshot;
    private String status;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
