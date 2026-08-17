package org.example.merchant_ai_operation.promotion.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//这个对象专门作为消费者查询结果返回
public record PromotionReservationDetailVO(
        String reservationId,
        Long activityItemId,
        Integer quantity,
        BigDecimal unitPriceSnapshot,
        String reservationStatus,
        Long orderId,
        String orderNo,
        String orderStatus,
        BigDecimal totalAmount,
        LocalDateTime expireAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
