package org.example.merchant_ai_operation.order.mapper;

import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.order.entity.CheckoutGroup;


@Mapper
public interface CheckoutGroupMapper {

    @Insert("""
            INSERT INTO checkout_group (
                checkout_no,
                consumer_id,
                status,
                total_amount
            ) VALUES (
                #{checkoutNo},
                #{consumerId},
                #{status},
                #{totalAmount}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    //插入跨店订单进数据库
    int insert(CheckoutGroup checkoutGroup);

    @Select("""
            SELECT
                id,
                checkout_no AS checkoutNo,
                consumer_id AS consumerId,
                status,
                total_amount AS totalAmount,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM checkout_group
            WHERE id = #{checkoutGroupId}
              AND consumer_id = #{consumerId}
            """)
    //以后用户打开“结算详情”或“我的结算记录”时，需要通过id和消费者id来查询
    CheckoutGroup selectByIdAndConsumerId(
            @Param("checkoutGroupId") Long checkoutGroupId,
            @Param("consumerId") Long consumerId
    );


}
