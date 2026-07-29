package org.example.merchant_ai_operation.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于等于1")
        Integer quantity
) {
}
