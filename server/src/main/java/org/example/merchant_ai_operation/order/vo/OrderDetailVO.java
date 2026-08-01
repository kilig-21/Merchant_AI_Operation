package org.example.merchant_ai_operation.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

//OrderDetailVO 表示一整笔订单，里面包含一个 items 列表。
public record OrderDetailVO(
        Long id,
        String orderNo,
        Long tenantId,
        String status,
        BigDecimal totalAmount,
        LocalDateTime expireAt,
        LocalDateTime createdAt,
        List<OrderItemVO> items
) {
}
