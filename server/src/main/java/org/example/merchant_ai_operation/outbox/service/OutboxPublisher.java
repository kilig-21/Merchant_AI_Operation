package org.example.merchant_ai_operation.outbox.service;


import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.example.merchant_ai_operation.config.RabbitMqConfig;
import org.example.merchant_ai_operation.outbox.entity.OutboxEvent;
import org.example.merchant_ai_operation.outbox.mapper.OutboxEventMapper;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*
* 每5秒执行一次
→ 查询最多100条 PENDING 事件
→ 发布到延迟队列
→ RabbitMQ 确认
→ 改成 PUBLISHED
* */

@Component
@Slf4j
public class OutboxPublisher {

    private final OutboxEventMapper outboxEventMapper;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxEventMapper outboxEventMapper, RabbitTemplate rabbitTemplate) {
        this.outboxEventMapper = outboxEventMapper;
        this.rabbitTemplate = rabbitTemplate;
    }


    /**
     * 定时扫描待发布的 Outbox 事件，并尝试发送到 RabbitMQ。
     * fixedDelay = 5000 表示：
     * 上一次方法执行结束后，再等待 5 秒执行下一次。
     */
    //具体设置某个任务什么时候执行
    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        try {
            log.info("OutboxPublisher 开始执行");

            List<OutboxEvent> events =
                    outboxEventMapper.selectPendingEvents(100);

            log.info("待发布事件数量：{}", events.size());

            for (OutboxEvent event : events) {
                publishOne(event);
            }
        } catch (Exception e) {
            log.error("查询 Outbox 事件失败", e);
        }
    }

    /**
     * 发布单条 Outbox 事件。
     * 发布成功：确认 RabbitMQ 已接收，并更新事件状态。
     * 发布失败：保留待发布状态，等待下次定时任务重试。
     *
     * @param event 待发布的 Outbox 事件
     */
    private void publishOne(OutboxEvent event) {
        try {
            log.info("开始发布事件，eventId={}", event.getEventId());

            CorrelationData correlationData =
                    new CorrelationData(event.getEventId());

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.ORDER_EXCHANGE,
                    RabbitMqConfig.ORDER_CREATED_KEY,
                    event.getPayload(),
                    message -> {
                        message.getMessageProperties()
                                .setMessageId(event.getEventId());
                        message.getMessageProperties()
                                .setType(event.getEventType());
                        message.getMessageProperties()
                                .setContentType("application/json");
                        message.getMessageProperties()
                                .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    },
                    correlationData
            );

            CorrelationData.Confirm confirm =
                    correlationData.getFuture()
                            .get(5, TimeUnit.SECONDS);

            if (confirm.isAck()) {
                int updated = outboxEventMapper.markPublished(
                        event.getId(),
                        event.getEventId()
                );
                log.info( "Outbox 发布成功，更新行数：{}", updated);
            } else {
                log.info( "RabbitMQ 确认失败：{}", confirm.getReason());
            }

        } catch (Exception e) {
            log.error("发布事件失败，eventId={}", event.getEventId(), e);
        }
    }
}