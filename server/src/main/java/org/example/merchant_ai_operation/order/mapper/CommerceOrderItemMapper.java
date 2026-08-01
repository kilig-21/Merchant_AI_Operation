package org.example.merchant_ai_operation.order.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.merchant_ai_operation.order.entity.CommerceOrderItem;
import org.apache.ibatis.annotations.Param;
import org.example.merchant_ai_operation.order.vo.OrderItemVO;

import java.util.List;

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

    @Select("""
            SELECT
                id,
                order_id AS orderId,
                sku_id AS skuId,
                sku_name_snapshot AS skuNameSnapshot,
                sale_price AS salePrice,
                quantity
                FROM commerce_order_item
                WHERE order_id = #{orderId}
            """)
    //为了支付成功后知道这笔订单买了哪些 SKU、每个买了几个。返回成列表出来
    //selectByOrderId(...) 返回 CommerceOrderItem，给支付逻辑用，偏内部业务处理。
    List<CommerceOrderItem> selectByOrderId(
            @Param("orderId") Long orderId
    );

    @Select("""
            SELECT
                id,
                sku_id AS skuId,
                sku_name_snapshot AS skuNameSnapshot,
                sale_price AS salePrice,
                quantity
            FROM commerce_order_item
            WHERE order_id = #{orderId}
            """)
    //selectItemVOByOrderId(...) 返回 OrderItemVO，给接口展示用，字段刚好对前端友好。
    List<OrderItemVO> selectItemVOByOrderId(@Param("orderId") Long orderId);


}