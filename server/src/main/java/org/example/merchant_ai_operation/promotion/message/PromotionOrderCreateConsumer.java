package org.example.merchant_ai_operation.promotion.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.example.merchant_ai_operation.config.RabbitMqConfig;
import org.example.merchant_ai_operation.promotion.dto.PromotionOrderCreateEvent;
import org.example.merchant_ai_operation.promotion.service.PromotionOrderCreationService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromotionOrderCreateConsumer {

    private final ObjectMapper objectMapper;
    private final PromotionOrderCreationService promotionOrderCreationService;

    public PromotionOrderCreateConsumer(
            ObjectMapper objectMapper,
            PromotionOrderCreationService promotionOrderCreationService
    ) {
        this.objectMapper = objectMapper;
        this.promotionOrderCreationService = promotionOrderCreationService;
    }

    @RabbitListener(queues = RabbitMqConfig.PROMOTION_ORDER_CREATE_QUEUE)
    public void handlePromotionOrderCreate(
            Message message,
            Channel channel
    ) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            PromotionOrderCreateEvent event = objectMapper.readValue(
                    payload,
                    PromotionOrderCreateEvent.class
            );

            if (event.reservationId() == null || event.reservationId().isBlank()) {
                throw new IllegalArgumentException("促销建单消息缺少 reservationId");
            }

            promotionOrderCreationService.createOrderFromReservation(
                    event.reservationId()
            );

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicReject(deliveryTag, false);
        }
    }
}