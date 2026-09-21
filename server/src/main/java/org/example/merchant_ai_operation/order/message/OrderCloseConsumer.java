package org.example.merchant_ai_operation.order.message;


import tools.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.example.merchant_ai_operation.config.RabbitMqConfig;
import org.example.merchant_ai_operation.order.service.OrderCloseService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
public class OrderCloseConsumer {
    private final ObjectMapper objectMapper;
    private final OrderCloseService orderCloseService;

    public OrderCloseConsumer(
            ObjectMapper objectMapper,
            OrderCloseService orderCloseService
    ) {
        this.objectMapper = objectMapper;
        this.orderCloseService = orderCloseService;
    }

    //监听RabbitMqConfig.ORDER_CLOSE_QUEUE的队列:有消息就调用handleOrderClose()这个方法
    @RabbitListener(queues = RabbitMqConfig.ORDER_CLOSE_QUEUE)
    public void handleOrderClose(Message message, Channel channel) throws IOException {
        //取到 RabbitMQ 给这条消息的“签收编号”，后面 ack/reject 都靠它定位是哪条消息。
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            //RabbitMQ 的消息正文本质是字节数组；这句把它按 UTF-8 还原成 JSON 字符串。
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);

            //把 JSON 转成 Java 的 OrderCloseMessage 对象
            OrderCloseMessage closeMessage = objectMapper.readValue(payload, OrderCloseMessage.class);
            if (closeMessage.orderId() == null) {
                throw new IllegalArgumentException("订单关闭消息缺少 orderId");
            }

            //这里才是“真正执行关单”的位置条件更新为 CLOSED → 释放锁定库存 → 写 ORDER_CLOSE 库存流水
            orderCloseService.closeExpiredOrder(closeMessage.orderId());

            //意思是：这条消息已经办完，请 RabbitMQ 从关闭队列移除。
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            //意思是：这条消息处理失败了，我拒绝签收它。
            channel.basicReject(deliveryTag, false);
        }

    }

}
