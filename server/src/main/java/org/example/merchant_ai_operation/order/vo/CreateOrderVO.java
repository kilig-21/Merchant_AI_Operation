package org.example.merchant_ai_operation.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//orderId：系统内部用，像身份证数据库主键
//orderNo：业务外部用，像快递单号/订单编号

public record CreateOrderVO(
        Long orderId,//数据库内部 ID
        String orderNo,//业务订单号
        String status,
        BigDecimal totalAmount,
        LocalDateTime expireAt

){

}
