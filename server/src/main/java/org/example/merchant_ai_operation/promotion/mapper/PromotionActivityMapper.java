package org.example.merchant_ai_operation.promotion.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.promotion.entity.PromotionActivity;
import org.example.merchant_ai_operation.promotion.entity.PromotionItem;

import java.time.LocalDateTime;

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
    //根据活动ID查询活动规则
    //→ 查活动主信息
    //→ 开始时间、结束时间、状态
    PromotionActivity selectByIdAndTenantId(
            @Param("activityId") Long activityId,
            @Param("tenantId") Long tenantId
    );
}
