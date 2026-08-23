package org.example.merchant_ai_operation.order.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.order.entity.CommerceOrder;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommerceOrderMapper {

    @Insert("""
            INSERT INTO commerce_order (
                order_no,
                tenant_id,
                consumer_id,
                checkout_group_id,
                status,
                total_amount,
                expire_at
            )
            VALUES (
                #{orderNo},
                #{tenantId},
                #{consumerId},
                #{checkoutGroupId},
                #{status},
                #{totalAmount},
                #{expireAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    //增加订单
    int insert(CommerceOrder order);

    @Select("""
            SELECT
                id,
                order_no AS orderNo,
                tenant_id AS tenantId,
                consumer_id AS consumerId,
                checkout_group_id AS checkoutGroupId,
                status,
                total_amount AS totalAmount,
                expire_at AS expireAt,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM commerce_order
            WHERE consumer_id = #{consumerId}
            ORDER BY created_at DESC
            """)
    //这个是订单列表。现在先不分页
    //查我(消费者)的订单列表
    //通过消费者查询
    List<CommerceOrder> selectByConsumerId(@Param("consumerId") Long consumerId);

    @Select("""
            SELECT
                id,
                order_no AS orderNo,
                tenant_id AS tenantId,
                consumer_id AS consumerId,
                checkout_group_id AS checkoutGroupId,
                status,
                total_amount AS totalAmount,
                expire_at AS expireAt,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM commerce_order
            WHERE id = #{orderId}
              AND consumer_id = #{consumerId}
            """)
    //不是只靠 id 查。这样消费者 A 拿到消费者 B 的订单 ID，也查不到。
    //依据订单和我(消费者)来查询订单详情;
    //通过订单id和消费者id查询
    CommerceOrder selectByOrderIdAndConsumerId(
            @Param("orderId")  Long orderId,
            @Param("consumerId")   Long consumerId
    );

    @Update("""
            UPDATE commerce_order
            SET status = 'CANCELLED'
            WHERE id = #{orderId}
              AND consumer_id = #{consumerId}
              AND status = 'PENDING_PAYMENT'
            """)
    //通过订单号和消费者id标记取消订单
    int markCancelledByIdAndConsumerId(
            @Param("orderId") Long orderId,
            @Param("consumerId") Long consumerId
    );

    @Update("""
        UPDATE commerce_order
        SET status = 'PAID'
        WHERE id = #{orderId}
          AND consumer_id = #{consumerId}
          AND status = 'PENDING_PAYMENT'
          AND expire_at > #{now}
        """)
    //更改支付的状态,支付完后就应该改成paid,否则不满足支付逻辑;
    int markPaidByIdAndConsumerId(
            @Param("orderId") Long orderId,
            @Param("consumerId") Long consumerId,
            @Param("now") LocalDateTime now
    );

    @Update("""
            UPDATE commerce_order
            SET status = 'CLOSED'
            WHERE id = #{orderId}
              AND status = 'PENDING_PAYMENT'
              AND expire_at <= #{now}
            """)
    // 条件关单：仅待支付且已过期的订单可以关闭。
    // 返回 1 表示当前线程获得关单资格；返回 0 时绝不能释放库存或写 ORDER_CLOSE 流水。
    int markClosedIfPendingAndExpired(
            @Param("orderId") Long orderId,
            @Param("now") LocalDateTime now
    );

    // ==================== 超时关单内部查询（MQ 消费 + 定时兜底） ==================== //

    @Select("""
        SELECT
            id,
            order_no AS orderNo,
            tenant_id AS tenantId,
            consumer_id AS consumerId,
            checkout_group_id AS checkoutGroupId,
            status,
            total_amount AS totalAmount,
            expire_at AS expireAt,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM commerce_order
        WHERE id = #{orderId}
        """)
    //给系统内部的 MQ 消费者和兜底扫描使用
    CommerceOrder selectById(@Param("orderId") Long orderId);

    @Select("""
        SELECT id
        FROM commerce_order
        WHERE status = 'PENDING_PAYMENT'
          AND expire_at <= #{now}
        ORDER BY expire_at, id
        LIMIT #{limit}
        """)
    //兜底任务的待处理订单清单查询”，只找，不关。
    //从数据库找出“已经过期、但仍然是待支付状态”的订单 ID。
    List<Long> selectExpiredPendingOrderIds(
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );
}
