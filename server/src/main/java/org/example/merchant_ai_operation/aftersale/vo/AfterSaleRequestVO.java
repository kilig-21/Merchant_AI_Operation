package org.example.merchant_ai_operation.aftersale.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AfterSaleRequestVO(
        Long id,
        String requestNo,
        Long orderId,
        Long orderItemId,
        Integer quantity,
        BigDecimal requestedAmount,
        String reason,
        String status,
        String merchantRemark,
        LocalDateTime decidedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
