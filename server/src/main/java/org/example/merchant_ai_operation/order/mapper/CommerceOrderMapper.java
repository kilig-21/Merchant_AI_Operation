package org.example.merchant_ai_operation.order.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.example.merchant_ai_operation.order.entity.CommerceOrder;

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

    int insert(CommerceOrder order);


}
