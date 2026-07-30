package org.example.merchant_ai_operation.order.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.example.merchant_ai_operation.order.entity.CommerceOrderItem;

@Mapper
public interface CommerceOrderItemMapper {

    @Insert("""
            INSERT INTO commerce_order_item (
                order_id,
                sku_id,
                sku_name_snapshot,
                sale_price,
                quantity
            )
            VALUES (
                #{orderId},
                #{skuId},
                #{skuNameSnapshot},
                #{salePrice},
                #{quantity}
            )
            """)
    int insert(CommerceOrderItem item);
}