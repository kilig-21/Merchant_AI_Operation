package org.example.merchant_ai_operation.cart.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CartItem {
    private Long id;
    private Long consumerId;
    private Long skuId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}