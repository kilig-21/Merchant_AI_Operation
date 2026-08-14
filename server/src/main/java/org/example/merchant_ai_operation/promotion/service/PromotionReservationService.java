package org.example.merchant_ai_operation.promotion.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.outbox.entity.OutboxEvent;
import org.example.merchant_ai_operation.outbox.mapper.OutboxEventMapper;
import org.example.merchant_ai_operation.promotion.dto.PromotionOrderCreateEvent;
import org.example.merchant_ai_operation.promotion.entity.PromotionItem;
import org.example.merchant_ai_operation.promotion.entity.PromotionReservation;
import org.example.merchant_ai_operation.promotion.mapper.PromotionItemMapper;
import org.example.merchant_ai_operation.promotion.mapper.PromotionReservationMapper;
import org.example.merchant_ai_operation.security.CurrentUser;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.example.merchant_ai_operation.promotion.dto.PromotionReservationResult;
import org.example.merchant_ai_operation.promotion.dto.ReservePromotionRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class PromotionReservationService {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> promotionReserveScript;
    private final Clock applicationClock;
    private final PromotionItemMapper promotionItemMapper;
    private final PromotionReservationMapper promotionReservationMapper;
    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;

    public PromotionReservationService(
            StringRedisTemplate stringRedisTemplate,
            DefaultRedisScript<List> promotionReserveScript,
            Clock applicationClock,
            PromotionItemMapper promotionItemMapper,
            PromotionReservationMapper promotionReservationMapper,
            OutboxEventMapper outboxEventMapper,
            ObjectMapper objectMapper
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.promotionReserveScript = promotionReserveScript;
        this.applicationClock = applicationClock;
        this.promotionItemMapper = promotionItemMapper;
        this.promotionReservationMapper = promotionReservationMapper;
        this.outboxEventMapper = outboxEventMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PromotionReservationResult reserve(ReservePromotionRequest request) {

        // 从安全上下文和抢购请求取得执行 Lua 所需的用户、活动商品、资格编号与服务端时间参数
        Long consumerId = CurrentUser.requiredConsumerId();
        Long itemId = request.activityItemId();
        String reservationId = UUID.randomUUID().toString();
        long nowMillis = Instant.now(applicationClock).toEpochMilli();

        List<?> result = executeReservationScript(request, itemId, consumerId, nowMillis, reservationId);

        //result == null 要满足上线后lua是空白的
        if (result == null || result.size() != 2) {
            throw new IllegalStateException("抢购 Lua 脚本返回结果异常");
        }

        int code = Integer.parseInt(String.valueOf(result.get(0)));
        String resultReservationId = String.valueOf(result.get(1));

        // 返回结果不是约定的 {返回码, reservationId} 两项结构时，停止处理。
        switch (code) {
            case 1 -> {
                PromotionReservation reservation = savePendingReservation(
                        consumerId,
                        itemId,
                        request,
                        resultReservationId
                );
                persistPromotionOrderCreateOutboxEvent(reservation);
                return new PromotionReservationResult(code, resultReservationId);
            }
          case 2 -> {
              return new PromotionReservationResult(code, resultReservationId);
          }
            case -1 -> throw new BizException(409, "活动尚未预热");
            case -2 -> throw new BizException(409, "活动尚未开始");
            case -3 -> throw new BizException(409, "活动已结束");
            case -4 -> throw new BizException(409, "活动库存不足或已售罄");
            case -5 -> throw new BizException(409, "超过单用户限购");
            default -> throw new IllegalStateException("未知的抢购 Lua 返回码：" + code);
        }

    }


    //<-------------提取方法-------------->


    private PromotionReservation savePendingReservation(
            Long consumerId,
            Long itemId,
            ReservePromotionRequest request,
            String reservationId
    ){
        PromotionItem item = promotionItemMapper.selectById(itemId);
        if (item == null) {
            throw new BizException(404, "活动商品不存在");
        }

        //存入数据库内:
        PromotionReservation reservation = new PromotionReservation();
        reservation.setReservationId(reservationId);
        reservation.setActivityId(item.getActivityId());
        reservation.setActivityItemId(item.getId());
        reservation.setTenantId(item.getTenantId());
        reservation.setConsumerId(consumerId);
        reservation.setRequestKey(request.requestKey());
        reservation.setQuantity(request.quantity());
        reservation.setUnitPriceSnapshot(item.getActivityPrice());
        reservation.setStatus("PENDING_ORDER");

        if (promotionReservationMapper.insert(reservation) != 1) {
            throw new BizException(500, "保存抢购资格失败");
        }
        return reservation;
    }

    // 资格记录已先写入并获得自增 ID；Outbox 以该 ID 作为聚合标识。
    // 两条 MySQL 写入处于同一事务，任一失败都会一起回滚。
    private void persistPromotionOrderCreateOutboxEvent(PromotionReservation reservation) {

        // 只构造稳定的促销建单事件载荷，避免把整个可变实体直接发送给消息消费者。
        OutboxEvent event = new OutboxEvent();

        populatePromotionOrderCreateEvent(reservation, event);
        if (outboxEventMapper.insert(event) != 1) {
            throw new BizException(500, "保存促销订单事件失败");
        }
    }

    // 填充促销建单事件的元数据与稳定消息载荷。
    private void populatePromotionOrderCreateEvent(PromotionReservation reservation, OutboxEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setAggregateType("PROMOTION_RESERVATION");
        event.setAggregateId(reservation.getId());
        event.setEventType("PROMOTION_ORDER_CREATE");

        try {
            event.setPayload(objectMapper.writeValueAsString(
                    new PromotionOrderCreateEvent(
                            reservation.getReservationId()
                    )
            ));
        } catch (JsonProcessingException e) {
            throw new BizException(500, "促销订单事件生成失败");
        }
        event.setStatus("PENDING");
        event.setRetryCount(0);
        event.setNextRetryAt(LocalDateTime.now(applicationClock));
    }

    private  List<?> executeReservationScript(
            ReservePromotionRequest request,
            Long itemId,
            Long consumerId,
            long nowMillis,
            String reservationId
    ) {
        List<String> keys = List.of(
                rulesKey(itemId),
                stockKey(itemId),
                userQuantityKey(itemId, consumerId),
                requestKey(itemId, request.requestKey())
        );

        return stringRedisTemplate.execute(
                promotionReserveScript,
                keys,
                String.valueOf(nowMillis),
                String.valueOf(request.quantity()),
                reservationId
        );
    }

    //规则key
    private String rulesKey(Long itemId) {
        return "promotion:item:{" + itemId + "}:rules:v1";
    }

    //库存key
    private String stockKey(Long itemId) {
        return "promotion:item:{" + itemId + "}:stock:v1";
    }

    //用户数量key
    private String userQuantityKey(Long itemId, Long consumerId) {
        return "promotion:item:{" + itemId + "}:user:" + consumerId + ":v1";
    }

    //请求key
    private String requestKey(Long itemId, String requestKey) {
        return "promotion:item:{" + itemId + "}:request:" + requestKey + ":v1";
    }


}
