package org.example.merchant_ai_operation.aftersale.vo;

import java.math.BigDecimal;

public record AfterSaleOrderItemContext(
        Long orderId,
        Long orderItemId,
        Long tenantId,
        Long consumerId,
        String orderStatus,
        BigDecimal salePrice,
        Integer purchasedQuantity
) {
}
