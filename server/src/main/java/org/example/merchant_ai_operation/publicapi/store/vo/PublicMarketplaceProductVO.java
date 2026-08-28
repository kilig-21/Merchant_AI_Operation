package org.example.merchant_ai_operation.publicapi.store.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PublicMarketplaceProductVO (
        Long storeId,
        String storeName,
        Long id,
        String name,
        String description,
        BigDecimal minSalePrice,
        Integer totalAvailableStock,
        LocalDateTime updatedAt
){}
