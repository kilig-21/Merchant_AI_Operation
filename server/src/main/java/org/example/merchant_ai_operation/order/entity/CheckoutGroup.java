package org.example.merchant_ai_operation.order.entity;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CheckoutGroup {
    private Long id;
    private String checkoutNo;
    private Long consumerId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
