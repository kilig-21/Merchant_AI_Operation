package org.example.merchant_ai_operation.publicapi.product.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PublicProductListItemVO(
        Long id,
        String name,
        String description,//商品基础展示
        BigDecimal minSalePrice,
        Integer totalAvailableStock,
        LocalDateTime updatedAt //方便后续排序或展示
) {

}
