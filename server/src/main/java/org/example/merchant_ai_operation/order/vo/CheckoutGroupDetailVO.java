package org.example.merchant_ai_operation.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

//这个VO是“读取详情”的结果，子项要用 OrderDetailVO
public record CheckoutGroupDetailVO (
        Long checkoutGroupId,
        String checkoutNo,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderDetailVO> orders
) {
}
