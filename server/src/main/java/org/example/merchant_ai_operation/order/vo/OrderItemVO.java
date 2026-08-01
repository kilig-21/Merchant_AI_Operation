package org.example.merchant_ai_operation.order.vo;

import java.math.BigDecimal;


//OrderItemVO 表示订单里的每个 SKU。
public record OrderItemVO(
        Long id,
        Long skuId,
        String skuNameSnapshot,
        BigDecimal salePrice,
        Integer quantity
) {
}
