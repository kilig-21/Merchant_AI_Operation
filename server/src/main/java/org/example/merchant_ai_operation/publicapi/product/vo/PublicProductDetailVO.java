package org.example.merchant_ai_operation.publicapi.product.vo;

import java.time.LocalDateTime;
import java.util.List;


//PublicProductDetailVO 表示商品详情整体。
public record PublicProductDetailVO(
        Long id,
        String name,
        String description,
        LocalDateTime updatedAt,
        List<PublicSkuVO> skus
) {
}
