package org.example.merchant_ai_operation.order.mapper;

import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.order.entity.CommerceOrderAddress;

@Mapper
public interface CommerceOrderAddressMapper {

    @Insert("""
            INSERT INTO commerce_order_address (
                order_id,
                consumer_id,
                source_address_id,
                receiver_name,
                receiver_phone,
                province,
                city,
                district,
                detail_address
            ) VALUES (
                #{address.orderId},
                #{address.consumerId},
                #{address.sourceAddressId},
                #{address.receiverName},
                #{address.receiverPhone},
                #{address.province},
                #{address.city},
                #{address.district},
                #{address.detailAddress}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "address.id", keyColumn = "id")
    //将此次下单的地址写进快照订单地址里(不可变)
    int insert(@Param("address") CommerceOrderAddress address);

    @Select("""
            SELECT
                id,
                order_id AS orderId,
                consumer_id AS consumerId,
                source_address_id AS sourceAddressId,
                receiver_name AS receiverName,
                receiver_phone AS receiverPhone,
                province,
                city,
                district,
                detail_address AS detailAddress,
                created_at AS createdAt
            FROM commerce_order_address
            WHERE order_id = #{orderId}
            """)
    //通过订单id查看订单的地址
    CommerceOrderAddress selectByOrderId(@Param("orderId") Long orderId);
}