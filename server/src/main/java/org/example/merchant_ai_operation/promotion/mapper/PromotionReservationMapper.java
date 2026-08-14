package org.example.merchant_ai_operation.promotion.mapper;

import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.promotion.entity.PromotionReservation;

@Mapper
public interface PromotionReservationMapper {
    @Insert("""
        INSERT INTO promotion_reservations (
            reservation_id,
            activity_id,
            activity_item_id,
            tenant_id,
            consumer_id,
            request_key,
            quantity,
            unit_price_snapshot,
            status
        )
        VALUES (
            #{reservationId},
            #{activityId},
            #{activityItemId},
            #{tenantId},
            #{consumerId},
            #{requestKey},
            #{quantity},
            #{unitPriceSnapshot},
            #{status}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PromotionReservation reservation);

    @Select("""
            SELECT
                id,
                reservation_id AS reservationId,
                activity_id AS activityId,
                activity_item_id AS activityItemId,
                tenant_id AS tenantId,
                consumer_id AS consumerId,
                request_key AS requestKey,
                quantity,
                unit_price_snapshot AS unitPriceSnapshot,
                status,
                order_id AS orderId,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM promotion_reservations
            WHERE reservation_id = #{reservationId}
            FOR UPDATE
            """)
    //RabbitMQ 系统内部消费者使用的Mapper方法
    PromotionReservation selectByReservationIdForUpdate(
            @Param("reservationId") String reservationId
    );

    @Update("""
    UPDATE promotion_reservations
    SET status = 'ORDER_CREATED',
        order_id = #{orderId}
    WHERE reservation_id = #{reservationId}
      AND status = 'PENDING_ORDER'
    """)
    //标记订单已经创建
    //仅把待建单资格绑定到新订单；返回 1 才表示当前事务完成状态推进。
    int markOrderCreated(
            @Param("reservationId") String reservationId,
            @Param("orderId") Long orderId
    );

}
