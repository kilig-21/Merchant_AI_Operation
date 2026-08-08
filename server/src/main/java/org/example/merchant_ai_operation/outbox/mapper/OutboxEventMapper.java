package org.example.merchant_ai_operation.outbox.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.outbox.entity.OutboxEvent;

import java.util.List;

@Mapper
public interface OutboxEventMapper {

    @Insert("""
            INSERT INTO outbox_events (
                event_id,
                aggregate_type,
                aggregate_id,
                event_type,
                payload,
                status,
                retry_count,
                next_retry_at
            )
            VALUES (
                #{eventId},
                #{aggregateType},
                #{aggregateId},
                #{eventType},
                #{payload},
                #{status},
                #{retryCount},
                #{nextRetryAt}
            )
            """)
    //向 Outbox 表写事件
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OutboxEvent event);

    @Select("""
        SELECT
            id,
            event_id AS eventId,
            aggregate_type AS aggregateType,
            aggregate_id AS aggregateId,
            event_type AS eventType,
            payload,
            status,
            retry_count AS retryCount,
            next_retry_at AS nextRetryAt,
            published_at AS publishedAt,
            last_error AS lastError,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM outbox_events
        WHERE status = 'PENDING'
        ORDER BY id
        LIMIT #{limit}
        """)
    //  查询还没有发送的事件
    List<OutboxEvent> selectPendingEvents(@Param("limit") int limit);

    @Update("""
        UPDATE outbox_events
        SET status = 'PUBLISHED',
            published_at = NOW()
        WHERE id = #{id}
          AND event_id = #{eventId}
          AND status = 'PENDING'
        """)
    // 确认收到消息后把事件改成 PUBLISHED
    int markPublished(
            @Param("id") Long id,
            @Param("eventId") String eventId
    );

}
