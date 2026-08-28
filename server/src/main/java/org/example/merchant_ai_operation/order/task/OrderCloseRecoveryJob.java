package org.example.merchant_ai_operation.order.task;


import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;
import org.example.merchant_ai_operation.order.service.OrderCloseService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderCloseRecoveryJob {
    //静态变量
    private static final int BATCH_SIZE = 100;
    private static final Logger log = LoggerFactory.getLogger(OrderCloseRecoveryJob.class);

    //注入属性
    private final Clock applicationClock;
    private final CommerceOrderMapper commerceOrderMapper;
    private final OrderCloseService orderCloseService;
    public OrderCloseRecoveryJob(CommerceOrderMapper commerceOrderMapper, OrderCloseService orderCloseService, Clock applicationClock) {
        this.commerceOrderMapper = commerceOrderMapper;
        this.orderCloseService = orderCloseService;
        this.applicationClock = applicationClock;
    }


    @Scheduled(fixedDelay = 60_000)
    public void closeExpiredOrders() {
        //从数据库找出“已经过期、但仍然是待支付状态”的订单 ID。
        List<Long> orderIds = commerceOrderMapper.selectExpiredPendingOrderIds(
                LocalDateTime.now(applicationClock),
                BATCH_SIZE
        );

        for (Long orderId : orderIds) {
            try {
                orderCloseService.closeExpiredOrder(orderId);
            } catch (Exception e) {
                log.error("兜底关单失败，orderId={}", orderId, e);
            }
        }
    }


}
