package org.example.merchant_ai_operation.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        //防止前端不传 skuId 或 quantity。
        @NotNull(message = "SKU ID 不能为空")
        Long skuId,

        @NotNull(message="数量不能为空")
        //@Min(1)：购物车数量不能是 0 或负数。
        @Min(value=1,message = "数量必须大于等于1")
        Integer quantity
) {}
