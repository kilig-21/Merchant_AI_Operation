package org.example.merchant_ai_operation.cart.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.cart.entity.CartItem;
import org.example.merchant_ai_operation.cart.vo.CartItemDetailVO;

import java.util.List;


@Mapper
public interface CartItemMapper {

    @Select("""
            SELECT
                id,
                consumer_id AS consumerId,
                sku_id AS skuId,
                quantity,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM cart_item
            WHERE consumer_id = #{consumerId}
              AND sku_id = #{skuId}
            """)
    //selectByConsumerIdAndSkuId：先查购物车里有没有同一个 SKU。
    CartItem selectByConsumerIdAndSkuId(
            @Param("consumerId") Long consumerId,
            @Param("skuId") Long skuId
    );

    @Insert("""
            INSERT INTO cart_item (
                id,
                consumer_id,
                sku_id,
                quantity
            ) VALUES (
                #{item.id},
                #{item.consumerId},
                #{item.skuId},
                #{item.quantity}
            )
            """)
    //insert：没有就新增一条。
    int insert(@Param("item") CartItem item);

    @Update("""
            UPDATE cart_item
            SET quantity = quantity + #{deltaQuantity}
            WHERE id = #{id}
              AND consumer_id = #{consumerId}
            """)
    //increaseQuantity：有就把数量加上去。
    //也就是用户在商品详情页再次点击“加入购物车”。
    int increaseQuantity(
            @Param("id") Long id,
            @Param("consumerId") Long consumerId,
            @Param("deltaQuantity") Integer deltaQuantity
    );

    @Select("""
            SELECT
                id,
                consumer_id AS consumerId,
                sku_id AS skuId,
                quantity,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM cart_item
            WHERE id = #{id}
              AND consumer_id = #{consumerId}
            """)
    //selectByIdAndConsumerId：更新完或新增完后，再查一次当前消费者自己的购物车项，避免拿到别人的数据
    CartItem selectByIdAndConsumerId(
            @Param("id") Long id,
            @Param("consumerId") Long consumerId
    );

    @Select("""
        SELECT
            id,
            consumer_id AS consumerId,
            sku_id AS skuId,
            quantity,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM cart_item
        WHERE consumer_id = #{consumerId}
        ORDER BY updated_at DESC
        """)
    //列出商品列表
    List<CartItem> selectByConsumerId(@Param("consumerId") Long consumerId);

    @Update("""
        UPDATE cart_item
        SET quantity = #{quantity}
        WHERE id = #{id}
          AND consumer_id = #{consumerId}
        """)
    //在原来的数量上加上这次新增的数量。
    //就是直接再数量上修改成想要购买的数量
    int updateQuantityByIdAndConsumerId(
            @Param("id") Long id,
            @Param("consumerId") Long consumerId,
            @Param("quantity") Integer quantity
    );

    @Delete("""
        DELETE FROM cart_item
        WHERE id = #{id}
          AND consumer_id = #{consumerId}
        """)
    //删除购物车的商品记录
    int deleteByIdAndConsumerId(
            @Param("id") Long id,
            @Param("consumerId") Long consumerId
    );

    @Select("""
        SELECT
            c.id,
            c.sku_id AS skuId,
            p.id AS productId,
            p.name AS productName,
            s.sku_name AS skuName,
            t.id AS storeId,
            t.name AS storeName,
            s.sale_price AS salePrice,
            s.available_stock AS availableStock,
            c.quantity,
            CASE
                WHEN s.id IS NOT NULL
                 AND p.id IS NOT NULL
                 AND t.id IS NOT NULL
                 AND t.status = 1
                 AND p.status = 'ON_SALE'
                 AND s.status = 'ON_SALE'
                 AND s.available_stock >= c.quantity
                THEN TRUE
                ELSE FALSE
            END AS purchasable,
            CASE
                WHEN s.id IS NULL THEN '商品规格已不存在'
                WHEN p.id IS NULL THEN '商品已不存在'
                WHEN t.id IS NULL OR t.status <> 1 THEN '店铺暂不可用'
                WHEN p.status <> 'ON_SALE' THEN '商品已下架'
                WHEN s.status <> 'ON_SALE' THEN '商品规格已下架'
                WHEN s.available_stock < c.quantity THEN '库存不足'
                ELSE NULL
            END AS unavailableReason
        FROM cart_item c
        LEFT JOIN product_sku s
          ON s.id = c.sku_id
        LEFT JOIN product_spu p
          ON p.id = s.spu_id
         AND p.tenant_id = s.tenant_id
        LEFT JOIN tenant t
          ON t.id = s.tenant_id
        WHERE c.consumer_id = #{consumerId}
        ORDER BY c.updated_at DESC, c.id DESC
        """)
    //读取购物车详情
    List<CartItemDetailVO> selectDetailsByConsumerId(
            @Param("consumerId") Long consumerId
    );




}