package org.example.merchant_ai_operation.merchant.analytics.vo;

import java.math.BigDecimal;

/**
 * 当前低库存 SKU 的只读快照。
 */
public record LowStockSkuVO(
        Long skuId,
        Long spuId,
        String productName,
        String skuName,
        BigDecimal salePrice,
        Integer availableStock,
        Integer lockedStock
) {
}
