package org.example.merchant_ai_operation.order.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommerceOrderItem {
    private Long id;
    private Long orderId;
    private Long skuId;
    private String skuNameSnapshot;
    private BigDecimal salePrice;
    private Integer quantity;
    private LocalDateTime createdAt;
}