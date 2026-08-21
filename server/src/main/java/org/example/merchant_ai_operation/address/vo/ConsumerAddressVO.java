package org.example.merchant_ai_operation.address.vo;

import java.time.LocalDateTime;

public record ConsumerAddressVO (
        Long id,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddress,
        Boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){}
