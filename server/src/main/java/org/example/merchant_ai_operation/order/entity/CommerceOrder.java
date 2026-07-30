package org.example.merchant_ai_operation.order.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommerceOrder {
    private Long id;
    private String orderNo;
    private Long tenantId;
    private Long consumerId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}