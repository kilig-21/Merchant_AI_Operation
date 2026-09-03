package org.example.merchant_ai_operation.cart.vo;

import java.math.BigDecimal;

//购物车细节VO
public record CartItemDetailVO (
        Long id,
        Long skuId,
        Long productId,
        String productName,
        String skuName,
        Long storeId,
        String storeName,
        BigDecimal salePrice,
        Integer availableStock,
        Integer quantity,
        Boolean purchasable,
        String unavailableReason
){}
