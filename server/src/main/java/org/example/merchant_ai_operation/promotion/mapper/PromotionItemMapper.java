package org.example.merchant_ai_operation.promotion.mapper;

import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.promotion.entity.PromotionItem;


@Mapper
public interface PromotionItemMapper {

    @Insert("""
            INSERT INTO promotion_items (
                activity_id,
                tenant_id,
                sku_id,
                activity_price,
                stock_total,
                stock_available,
                limit_per_user
            )
            VALUES (
                #{activityId},
                #{tenantId},
                #{skuId},
                #{activityPrice},
                #{stockTotal},
                #{stockAvailable},
                #{limitPerUser}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PromotionItem item);

    @Select("""
                SELECT
                    id,
                    tenant_id AS tenantId,
                    sale_price AS salePrice,
                    available_stock AS availableStock,
                    locked_stock AS lockedStock,
                    status
                FROM product_sku
                WHERE id = #{skuId}
                  AND tenant_id = #{tenantId}
            """)
    //查询活动中的SKU商品
    ProductSku selectSkuForPromotion(
            @Param("skuId") Long skuId,
            @Param("tenantId") Long tenantId
    );

    @Select("""
            SELECT
                id,
                activity_id AS activityId,
                tenant_id AS tenantId,
                sku_id AS skuId,
                activity_price AS activityPrice,
                stock_total AS stockTotal,
                stock_available AS stockAvailable,
                limit_per_user AS limitPerUser
            FROM promotion_items
            WHERE activity_id = #{activityId}
              AND tenant_id = #{tenantId}
            """)
    //归还哪个 SKU、归还多少库存
    //归还库存”之前，先要查出：这场活动占用了哪个 SKU，以及还剩多少活动库存可以归还
    PromotionItem selectByActivityId(
            @Param("activityId") Long activityId,
            @Param("tenantId") Long tenantId
    );

    @Update("""
            UPDATE promotion_items
            SET stock_available = 0
            WHERE activity_id = #{activityId}
              AND tenant_id = #{tenantId}
              AND stock_available > 0
            """)
    //取消后把活动自身可用库存清零，避免“库存归还了但活动表里还显示可卖”的数据矛盾
    int clearAvailableStock(
            @Param("activityId") Long activityId,
            @Param("tenantId") Long tenantId
    );

    @Select("""
            SELECT
                id,
                activity_id AS activityId,
                tenant_id AS tenantId,
                sku_id AS skuId,
                activity_price AS activityPrice,
                stock_total AS stockTotal,
                stock_available AS stockAvailable,
                limit_per_user AS limitPerUser
            FROM promotion_items
            WHERE id = #{itemId}
            """)
    PromotionItem selectById(@Param("itemId") Long itemId);

    @Update("""
        UPDATE promotion_items
        SET stock_available = stock_available - #{quantity}
        WHERE id = #{itemId}
          AND tenant_id = #{tenantId}
          AND stock_available >= #{quantity}
        """)
    // 异步建单时同步扣减活动库存；返回 1 才允许继续建单。
    int deductAvailableStockForReservation(
            @Param("itemId") Long itemId,
            @Param("tenantId") Long tenantId,
            @Param("quantity") Integer quantity
    );
}