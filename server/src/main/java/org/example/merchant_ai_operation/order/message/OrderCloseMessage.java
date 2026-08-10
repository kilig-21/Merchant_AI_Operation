package org.example.merchant_ai_operation.order.message;

import java.time.LocalDateTime;

/*
它不是订单实体，而是消息里允许传递的稳定数据结构。
消费者最终只信任 orderId，再回查数据库；orderNo 和 expireAt 用于日志与排查，不直接决定是否关单
* */
public record OrderCloseMessage (
        Long orderId,
        String orderNo,
        LocalDateTime expireAt
){}
