package org.example.merchant_ai_operation.order.vo;

import java.math.BigDecimal;
import java.util.List;

//返回的是“刚创建成功”的命令结果,子项是 CreateOrderVO；
public record CreateCheckoutGroupVO(
        Long checkoutGroupId,
        String checkoutNo,
        String status,
        BigDecimal totalAmount,
        List<CreateOrderVO> orders
){}
