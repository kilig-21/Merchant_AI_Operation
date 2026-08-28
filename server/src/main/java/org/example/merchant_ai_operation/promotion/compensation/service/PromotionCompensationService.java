package org.example.merchant_ai_operation.promotion.compensation.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.promotion.compensation.entity.PromotionCompensationRecord;
import org.example.merchant_ai_operation.promotion.compensation.mapper.PromotionCompensationRecordMapper;
import org.example.merchant_ai_operation.promotion.entity.PromotionReservation;
import org.example.merchant_ai_operation.promotion.mapper.PromotionReservationMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromotionCompensationService {
    public static final String ORDER_CREATE_FAILURE =
            "ORDER_CREATE_FAILURE";

    private final PromotionCompensationRecordMapper compensationRecordMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> promotionCompensateScript;
    private final PromotionReservationMapper promotionReservationMapper;

    public PromotionCompensationService(
            StringRedisTemplate stringRedisTemplate,

            @Qualifier("promotionCompensateScript")
            DefaultRedisScript<List> promotionCompensateScript,

            PromotionCompensationRecordMapper compensationRecordMapper,
            PromotionReservationMapper promotionReservationMapper
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.promotionCompensateScript = promotionCompensateScript;
        this.compensationRecordMapper = compensationRecordMapper;
        this.promotionReservationMapper = promotionReservationMapper;
    }

    //暂停当前事务，重新开启一个全新的独立事务。
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PromotionCompensationRecord createPendingOrderCreateFailure(
            PromotionReservation reservation,
            String reason
    ) {
        //先看记录表里有没有
        PromotionCompensationRecord record =
                compensationRecordMapper.selectByReservationIdAndType(
                reservation.getReservationId(),
                ORDER_CREATE_FAILURE
        );

        if (record != null && "COMPLETED".equals(record.getStatus())) {
            return record;
        }

        if (record == null) {
            record = new PromotionCompensationRecord();
            //写入补偿信息
            populateCompensationRecord(reservation, reason, record);

            if (compensationRecordMapper.insert(record) != 1) {
                throw new BizException(500, "保存促销补偿记录失败");
            }
        }

        /*
        * 1：本次补偿成功；
        * 2：补偿标记已存在，说明之前已经补偿过，也视为成功；
        * */
        return record;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executePendingCompensation(PromotionCompensationRecord record
    ){
        if (record == null || "COMPLETED".equals(record.getStatus())) {return;}

        //检测lua后的结果
        processCompensationRecord(record);//返回 1 或 2;
    }


    //调用 Redis 补偿 Lua 脚本(真正执行脚本的地方)
    public List<?> executeCompensationScript(PromotionCompensationRecord record){
        Long itemId = record.getActivityItemId();
        Long consumerId = record.getConsumerId();

        //创建Keys
        List<String> keys = List.of(
                stockKey(itemId),
                userQuantityKey(itemId, consumerId),
                compensationMarkerKey(itemId, record.getReservationId())
        );
        //调用 Redis 补偿 Lua 脚本
        return stringRedisTemplate.execute(
                promotionCompensateScript,
                keys,
                String.valueOf(record.getQuantity()),
                record.getReservationId()
        );

    }


    //Keys:
    //库存Key:
    private String stockKey(Long itemId) {
        return "promotion:item:{" + itemId + "}:stock:v1";
    }

    //数量Key:
    private String userQuantityKey(Long itemId, Long consumerId) {
        return "promotion:item:{" + itemId + "}:user:" + consumerId + ":v1";
    }

    //标记状态Key:
    private String compensationMarkerKey(Long itemId, String reservationId) {
        return "promotion:item:{" + itemId + "}:compensation:"
                + reservationId + ":v1";
    }

    //写入entity里
    private static void populateCompensationRecord(PromotionReservation reservation, String reason, PromotionCompensationRecord record) {
        record.setReservationId(reservation.getReservationId());
        record.setActivityItemId(reservation.getActivityItemId());
        record.setTenantId(reservation.getTenantId());
        record.setConsumerId(reservation.getConsumerId());
        record.setCompensationType(ORDER_CREATE_FAILURE);
        record.setQuantity(reservation.getQuantity());

        // 后续 Redis 活动库存需要增加的数量
        record.setStockChange(reservation.getQuantity());

        // 后续 Redis 用户限购数量需要回退的数量
        record.setUserQuantityChange(-reservation.getQuantity());

        record.setReason(reason);
        record.setStatus("PENDING");
    }

    //执行 Redis 补偿并更新记录状态
    private void processCompensationRecord(PromotionCompensationRecord record) {
        List<?> result = executeCompensationScript(record);

        if (result == null || result.size() != 2) {
            throw new BizException(500, "促销补偿 Lua 返回结果异常");
        }

        int code = Integer.parseInt(String.valueOf(result.get(0)));

        if (code != 1 && code != 2) {
            throw new BizException(
                    500,
                    "促销补偿执行失败：" + String.valueOf(result.get(1))
            );
        }

        //补偿记录表的Mapper
        if (compensationRecordMapper.markCompleted(record.getId()) != 1) {
            throw new BizException(500, "更新促销补偿状态失败");
        }

        //资格保存的Mapper
        PromotionReservation reservation =
                promotionReservationMapper.selectByReservationIdForUpdate(
                        record.getReservationId()
                );

        if (reservation != null
                && promotionReservationMapper.markCompensated(
                record.getReservationId()
        ) != 1) {
            throw new BizException(500, "更新抢购资格补偿状态失败");
        }

        record.setStatus("COMPLETED");
    }
}
