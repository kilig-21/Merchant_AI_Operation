package org.example.merchant_ai_operation.promotion.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.promotion.entity.PromotionActivity;
import org.example.merchant_ai_operation.promotion.entity.PromotionItem;
import org.example.merchant_ai_operation.promotion.vo.MerchantPromotionActivityVO;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PromotionActivityMapper {

    @Insert("""
            INSERT INTO promotion_activities (
                tenant_id,
                name,
                start_at,
                end_at,
                status
            )
            VALUES (
                #{tenantId},
                #{name},
                #{startAt},
                #{endAt},
                #{status}
            )
            """)
    //创建活动
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PromotionActivity activity);

    @Select("""
        SELECT COUNT(1)
        FROM promotion_activities pa
        JOIN promotion_items pi
          ON pi.activity_id = pa.id
        WHERE pa.tenant_id = #{tenantId}
          AND pi.sku_id = #{skuId}
          AND pa.status IN ('SCHEDULED', 'ACTIVE')
          AND pa.start_at < #{endAt}
          AND pa.end_at > #{startAt}
        """)
    //数当前商家某个 SKU，在指定时间范围内，是否已经存在正在生效且时间重叠的促销活动。
    //0     没有冲突，可以创建
    //大于0  存在冲突，应该拒绝创建
    int countOverlappingActivities(
            @Param("tenantId") Long tenantId,
            @Param("skuId") Long skuId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Update("""
        UPDATE promotion_activities
        SET status = 'CANCELLED'
        WHERE id = #{activityId}
          AND tenant_id = #{tenantId}
          AND status = 'SCHEDULED'
          AND start_at > CURRENT_TIMESTAMP
        """)
    //返回 1 代表本次成功取消；返回 0 则代表活动不存在、不是自己的活动，或已经开始/已取消
    int cancelScheduledActivity(
            @Param("activityId") Long activityId,
            @Param("tenantId") Long tenantId
    );

    @Select("""
        SELECT
            id,
            tenant_id AS tenantId,
            name,
            start_at AS startAt,
            end_at AS endAt,
            status
        FROM promotion_activities
        WHERE id = #{activityId}
          AND tenant_id = #{tenantId}
        """)
    /*
    根据活动ID查询活动规则
    → 查活动主信息
    → 开始时间、结束时间、状态
    */
    PromotionActivity selectByIdAndTenantId(
            @Param("activityId") Long activityId,
            @Param("tenantId") Long tenantId
    );

    @Update("""
    UPDATE promotion_activities
    SET status = 'ACTIVE'
    WHERE status = 'SCHEDULED'
      AND start_at <= #{now}
      AND end_at > #{now}
    """)
    //标记活动待安排 -> 正在开始并传进时间
    int markScheduledAsActive(@Param("now") LocalDateTime now);

    @Update("""
    UPDATE promotion_activities
    SET status = 'ENDED'
    WHERE status IN ('SCHEDULED', 'ACTIVE')
      AND end_at <= #{now}
    """)
    //标记活动结束了并传进时间
    int markExpiredAsEnded ( @Param("now") LocalDateTime now);

    @Select("""
        SELECT
            pa.id AS activityId,
            pi.id AS activityItemId,
            pa.name,
            spu.name AS productName,
            pi.sku_id AS skuId,
            sku.sku_name AS skuName,
            pi.activity_price AS activityPrice,
            pi.stock_total AS stockTotal,
            pi.stock_available AS stockAvailable,
            pi.limit_per_user AS limitPerUser,
            pa.start_at AS startAt,
            pa.end_at AS endAt,
            pa.status
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
        WHERE pa.tenant_id = #{tenantId}
        ORDER BY pa.start_at DESC, pa.id DESC
        """)
    // 查询指定商家租户的全部促销活动及其活动商品信息。
    List<MerchantPromotionActivityVO> selectActivitiesByTenantId(
            @Param("tenantId") Long tenantId
    );
}
