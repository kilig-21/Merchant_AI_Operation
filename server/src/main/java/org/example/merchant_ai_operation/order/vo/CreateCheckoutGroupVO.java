package org.example.merchant_ai_operation.order.vo;

import java.math.BigDecimal;
import java.util.List;

public record CreateCheckoutGroupVO(
        Long checkoutGroupId,
        String checkoutNo,
        String status,
        BigDecimal totalAmount,
        List<CreateOrderVO> orders
){}
