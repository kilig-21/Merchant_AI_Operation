package org.example.merchant_ai_operation.publicapi.product.vo;

import java.time.LocalDateTime;

public record PublicProductBaseVO (
        Long id,
        String name,
        String description,
        LocalDateTime updatedAt
){
}
