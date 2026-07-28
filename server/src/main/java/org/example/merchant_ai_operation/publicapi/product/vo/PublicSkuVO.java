package org.example.merchant_ai_operation.publicapi.product.vo;

import java.math.BigDecimal;


//PublicSkuVO 表示详情下面的每个可选规格。
public record PublicSkuVO(
        Long id,
        String skuName,
        BigDecimal salePrice,
        Integer availableStock
) {
}
