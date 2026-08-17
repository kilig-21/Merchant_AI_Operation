package org.example.merchant_ai_operation.promotion.compensation.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.promotion.compensation.entity.PromotionCompensationRecord;

import java.util.List;


@Mapper
public interface PromotionCompensationRecordMapper {

    @Insert("""
            INSERT INTO promotion_compensation_records (
                reservation_id,
                activity_item_id,
                tenant_id,
                consumer_id,
                compensation_type,
                quantity,
                stock_change,
                user_quantity_change,
                reason,
                status
            )
            VALUES (
                #{reservationId},
                #{activityItemId},
                #{tenantId},
                #{consumerId},
                #{compensationType},
                #{quantity},
                #{stockChange},
                #{userQuantityChange},
                #{reason},
                #{status}
            )
            """)
    //新增一条补偿记录:写入补偿审计记录
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PromotionCompensationRecord record);

    @Select("""
            SELECT
                id,
                reservation_id AS reservationId,
                activity_item_id AS activityItemId,
                tenant_id AS tenantId,
                consumer_id AS consumerId,
                compensation_type AS compensationType,
                quantity,
                stock_change AS stockChange,
                user_quantity_change AS userQuantityChange,
                reason,
                status,
                created_at AS createdAt,
                completed_at AS completedAt,
                updated_at AS updatedAt
            FROM promotion_compensation_records
            WHERE reservation_id = #{reservationId}
              AND compensation_type = #{compensationType}
            """)
    //查看补偿记录,判断同一补偿是否已经存在，防止重复补偿
    PromotionCompensationRecord selectByReservationIdAndType(
            @Param("reservationId") String reservationId,
            @Param("compensationType") String compensationType
    );

    @Update("""
        UPDATE promotion_compensation_records
        SET status = 'COMPLETED',
            completed_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
          AND status = 'PENDING'
        """)
    //补偿成功后把补偿记录状态更改为 COMPLETED
    int markCompleted(@Param("id") Long id);

    @Select("""
        SELECT
            id,
            reservation_id AS reservationId,
            activity_item_id AS activityItemId,
            tenant_id AS tenantId,
            consumer_id AS consumerId,
            compensation_type AS compensationType,
            quantity,
            stock_change AS stockChange,
            user_quantity_change AS userQuantityChange,
            reason,
            status,
            created_at AS createdAt,
            completed_at AS completedAt,
            updated_at AS updatedAt
        FROM promotion_compensation_records
        WHERE status = 'PENDING'
        ORDER BY id
        LIMIT #{limit}
        """)
    //查询待处理补偿任务
    List<PromotionCompensationRecord> selectPendingRecords(
            @Param("limit") int limit
    );

}
