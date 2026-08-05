package org.example.merchant_ai_operation.idempotency.mapper;

import org.example.merchant_ai_operation.idempotency.entity.IdempotentRequest;
import org.apache.ibatis.annotations.*;

@Mapper
public interface IdempotentRequestMapper {

    @Insert("""
            INSERT INTO idempotent_request (
                consumer_id,
                request_key,
                request_hash,
                status
            )
            VALUES (
                #{consumerId},
                #{requestKey},
                #{requestHash},
                #{status}
            )
            """)
    //第一次看到这个幂等键时，插入一条 PROCESSING 记录。
    //开启自增主键,并写回到java对象里
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(IdempotentRequest request);


    @Select("""
            SELECT
                id,
                consumer_id AS consumerId,
                request_key AS requestKey,
                request_hash AS requestHash,
                status,
                order_id AS orderId,
                response_body AS responseBody,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM idempotent_request
            WHERE consumer_id = #{consumerId}
              AND request_key = #{requestKey}
            """)
    //重复请求来了，先查有没有旧记录。
    IdempotentRequest selectByConsumerIdAndRequestKey(
            @Param("consumerId") Long consumerId,
            @Param("requestKey") String requestKey
    );

    @Update("""
            UPDATE idempotent_request
            SET status = 'SUCCESS',
                order_id = #{orderId}
            WHERE id = #{id}
              AND status = 'PROCESSING'
            """)
    //订单创建成功后，把幂等记录从 PROCESSING 改成 SUCCESS，并绑定订单 ID
    void markSuccess(
            @Param("id") Long id,
            @Param("orderId") Long orderId
    );






}
