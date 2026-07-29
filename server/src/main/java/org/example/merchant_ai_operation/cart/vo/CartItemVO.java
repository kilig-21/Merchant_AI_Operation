package org.example.merchant_ai_operation.cart.vo;

public record CartItemVO(
        Long id,
        Long skuId,
        Integer quantity
) {
}