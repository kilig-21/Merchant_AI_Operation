package org.example.merchant_ai_operation.order.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "请选择要结算的购物车项")
        List<Long> cartItemIds,

        // S4 地址快照来源地址；旧订单流程可以暂时不传
        Long addressId
) {
        // 兼容现有测试和旧调用方
        public CreateOrderRequest(List<Long> cartItemIds) {
                this(cartItemIds, null);
        }
}