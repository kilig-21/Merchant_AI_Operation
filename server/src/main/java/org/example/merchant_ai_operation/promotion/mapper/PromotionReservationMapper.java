package org.example.merchant_ai_operation.promotion.mapper;

import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.promotion.entity.PromotionReservation;
import org.example.merchant_ai_operation.promotion.vo.PromotionReservationDetailVO;

import java.util.List;

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
    //将资格入数据库
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

    @Select("""
            SELECT
                pr.reservation_id AS reservationId,
                pr.activity_item_id AS activityItemId,
                pr.quantity,
                pr.unit_price_snapshot AS unitPriceSnapshot,
                pr.status AS reservationStatus,
                pr.order_id AS orderId,
                co.order_no AS orderNo,
                co.status AS orderStatus,
                co.total_amount AS totalAmount,
                co.expire_at AS expireAt,
                pr.created_at AS createdAt,
                pr.updated_at AS updatedAt
            FROM promotion_reservations pr
            LEFT JOIN commerce_order co
              ON co.id = pr.order_id
             AND co.consumer_id = pr.consumer_id
            WHERE pr.reservation_id = #{reservationId}
              AND pr.consumer_id = #{consumerId}
            """)
    //查询上面业务已经入库的资格返回的用户
    PromotionReservationDetailVO selectDetailByReservationIdAndConsumerId(
            @Param("reservationId") String reservationId,
            @Param("consumerId") Long consumerId
    );

    @Select("""
        SELECT
            pr.reservation_id AS reservationId,
            pr.activity_item_id AS activityItemId,
            pr.quantity,
            pr.unit_price_snapshot AS unitPriceSnapshot,
            pr.status AS reservationStatus,
            pr.order_id AS orderId,
            co.order_no AS orderNo,
            co.status AS orderStatus,
            co.total_amount AS totalAmount,
            co.expire_at AS expireAt,
            pr.created_at AS createdAt,
            pr.updated_at AS updatedAt
        FROM promotion_reservations pr
        LEFT JOIN commerce_order co
          ON co.id = pr.order_id
         AND co.consumer_id = pr.consumer_id
        WHERE pr.consumer_id = #{consumerId}
          AND pr.activity_id = #{activityId}
        ORDER BY pr.created_at DESC, pr.id DESC
        """)
    // 查询当前消费者在指定活动中的全部抢购资格及异步订单结果。
    List<PromotionReservationDetailVO> selectDetailsByActivityIdAndConsumerId(
            @Param("activityId") Long activityId,
            @Param("consumerId") Long consumerId
    );

    @Update("""
            UPDATE promotion_reservations
            SET status = 'COMPENSATED'
            WHERE reservation_id = #{reservationId}
              AND status = 'PENDING_ORDER'
            """)
    //这条抢购资格已经完成补偿，不再等待创建订单。将状态改为已经补偿了
    int markCompensated(@Param("reservationId") String reservationId);

}
