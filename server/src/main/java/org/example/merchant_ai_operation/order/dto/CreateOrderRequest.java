package org.example.merchant_ai_operation.order.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "请选择要结算的购物车项")
        List<Long> cartItemIds
) {
}
