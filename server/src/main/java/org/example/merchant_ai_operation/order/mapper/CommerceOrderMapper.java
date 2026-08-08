package org.example.merchant_ai_operation.order.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.order.entity.CommerceOrder;

import java.util.List;

@Mapper
public interface CommerceOrderMapper {

    @Insert("""
            INSERT INTO commerce_order (
                order_no,
                tenant_id,
                consumer_id,
                status,
                total_amount,
                expire_at
            )
            VALUES (
                #{orderNo},
                #{tenantId},
                #{consumerId},
                #{status},
                #{totalAmount},
                #{expireAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    //增加订单
    int insert(CommerceOrder order);



    @Update("""
        UPDATE commerce_order
        SET status = 'PAID'
        WHERE id = #{orderId}
          AND consumer_id = #{consumerId}
          AND status = 'PENDING_PAYMENT'
        """)
    //更改支付的状态,支付完后就应该改成paid,否则不满足支付逻辑;
    int markPaidByIdAndConsumerId(
            @Param("orderId") Long orderId,
            @Param("consumerId") Long consumerId
    );

    @Select("""
            SELECT
                id,
                order_no AS orderNo,
                tenant_id AS tenantId,
                consumer_id AS consumerId,
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
    List<CommerceOrder> selectByConsumerId(@Param("consumerId") Long consumerId);


    @Select("""
            SELECT
                id,
                order_no AS orderNo,
                tenant_id AS tenantId,
                consumer_id AS consumerId,
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
}
