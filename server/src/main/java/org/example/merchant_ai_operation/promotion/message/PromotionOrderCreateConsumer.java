package org.example.merchant_ai_operation.promotion.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.example.merchant_ai_operation.config.RabbitMqConfig;
import org.example.merchant_ai_operation.promotion.compensation.entity.PromotionCompensationRecord;
import org.example.merchant_ai_operation.promotion.compensation.service.PromotionCompensationService;
import org.example.merchant_ai_operation.promotion.dto.PromotionOrderCreateEvent;
import org.example.merchant_ai_operation.promotion.entity.PromotionReservation;
import org.example.merchant_ai_operation.promotion.mapper.PromotionReservationMapper;
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
    private final PromotionReservationMapper promotionReservationMapper;
    private final PromotionCompensationService promotionCompensationService;

    public PromotionOrderCreateConsumer(
            ObjectMapper objectMapper,
            PromotionOrderCreationService promotionOrderCreationService,
            PromotionReservationMapper promotionReservationMapper,
    PromotionCompensationService promotionCompensationService
    ) {
        this.objectMapper = objectMapper;
        this.promotionOrderCreationService = promotionOrderCreationService;
        this.promotionReservationMapper = promotionReservationMapper;
        this.promotionCompensationService = promotionCompensationService;
    }

    @RabbitListener(queues = RabbitMqConfig.PROMOTION_ORDER_CREATE_QUEUE)
    public void handlePromotionOrderCreate(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        PromotionOrderCreateEvent event = null;

        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            event = objectMapper.readValue(
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

            /* 链路:
            * 建单失败
            → 根据 reservationId 查资格
            → 确认资格仍是 PENDING_ORDER
            → 新事务写入 PENDING 补偿记录
            → RabbitMQ 消息 reject
            * */

            if (event != null
                    && event.reservationId() != null
                    && !event.reservationId().isBlank()
            ) {
                PromotionReservation reservation = promotionReservationMapper
                        .selectByReservationIdForUpdate(
                                event.reservationId()
                        );

                if (reservation != null
                        && "PENDING_ORDER".equals(reservation.getStatus())//只有资格仍然是 PENDING_ORDER，才登记补偿
                ) {
                    String reason = e.getMessage() == null
                            ? e.getClass().getSimpleName()
                            : e.getMessage();

                    PromotionCompensationRecord record =
                            promotionCompensationService
                                    .createPendingOrderCreateFailure(
                                            reservation,
                                            reason
                                    );
                    promotionCompensationService.executePendingCompensation(record);
                }
            }
            channel.basicReject(deliveryTag, false);
        }
    }
}