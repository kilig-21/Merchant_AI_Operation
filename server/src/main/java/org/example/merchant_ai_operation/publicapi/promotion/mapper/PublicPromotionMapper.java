package org.example.merchant_ai_operation.publicapi.promotion.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.merchant_ai_operation.publicapi.promotion.vo.PublicPromotionActivityItemVO;

@Mapper
public interface PublicPromotionMapper {

    @Select("""
            SELECT
                pa.id AS activityId,
                pi.id AS activityItemId,
                pa.name,
                spu.name AS productName,
                sku.sku_name AS skuName,
                pi.activity_price AS activityPrice,
                pa.start_at AS startAt,
                pa.end_at AS endAt,
                pa.status,
                CASE
                    WHEN pi.stock_available > 0 THEN 'AVAILABLE'
                    ELSE 'SOLD_OUT'
                END AS stockStatus,
                pi.limit_per_user AS limitPerUser
            FROM promotion_activities pa
            JOIN promotion_items pi
              ON pi.activity_id = pa.id
             AND pi.tenant_id = pa.tenant_id
            JOIN product_sku sku
              ON sku.id = pi.sku_id
             AND sku.tenant_id = pa.tenant_id
            JOIN product_spu spu
              ON spu.id = sku.spu_id
             AND spu.tenant_id = sku.tenant_id
            WHERE pa.status IN ('SCHEDULED', 'ACTIVE')
              AND sku.status = 'ON_SALE'
              AND spu.status = 'ON_SALE'
            ORDER BY
                CASE pa.status WHEN 'ACTIVE' THEN 0 ELSE 1 END,
                pa.start_at ASC,
                pa.id DESC
            """)
    //查询所有消费者可见的促销活动，返回 List<...>，给活动列表页使用。
    List<PublicPromotionActivityItemVO> selectVisibleActivities();

    @Select("""
            SELECT
                pa.id AS activityId,
                pi.id AS activityItemId,
                pa.name,
                spu.name AS productName,
                sku.sku_name AS skuName,
                pi.activity_price AS activityPrice,
                pa.start_at AS startAt,
                pa.end_at AS endAt,
                pa.status,
                CASE
                    WHEN pi.stock_available > 0 THEN 'AVAILABLE'
                    ELSE 'SOLD_OUT'
                END AS stockStatus,
                pi.limit_per_user AS limitPerUser
            FROM promotion_activities pa
            JOIN promotion_items pi
              ON pi.activity_id = pa.id
             AND pi.tenant_id = pa.tenant_id
            JOIN product_sku sku
              ON sku.id = pi.sku_id
             AND sku.tenant_id = pa.tenant_id
            JOIN product_spu spu
              ON spu.id = sku.spu_id
             AND spu.tenant_id = sku.tenant_id
            WHERE pa.id = #{activityId}
              AND pa.status IN ('SCHEDULED', 'ACTIVE')
              AND sku.status = 'ON_SALE'
              AND spu.status = 'ON_SALE'
            """)
    //一个活动 ID 查询单个活动，返回一个VO，给活动详情页使用。
    PublicPromotionActivityItemVO selectVisibleActivityById(
            @Param("activityId") Long activityId
    );
}