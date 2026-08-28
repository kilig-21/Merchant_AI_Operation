package org.example.merchant_ai_operation.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateCheckoutRequest(
        @NotEmpty(message = "请选择要结算的购物车项")
        List<Long> cartItemIds,

        @NotNull(message = "请选择收货地址")
        Long addressId
){
}
