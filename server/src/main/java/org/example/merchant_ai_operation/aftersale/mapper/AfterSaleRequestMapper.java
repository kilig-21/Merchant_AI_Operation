package org.example.merchant_ai_operation.aftersale.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.aftersale.entity.AfterSaleRequest;

import java.time.LocalDateTime;
import java.util.List;

/*
负责售后申请当前状态和业务数据，例如：
- 新增售后申请；
- 查询售后详情；
- 查询消费者的申请列表；
- 查询商家租户下的申请列表；
- 修改状态；
- 保存商家审核备注、审核人、审核时间。
它关注的是：
这条售后申请现在是什么状态？
*/

@Mapper
public interface AfterSaleRequestMapper {

    @Insert("""
        INSERT INTO after_sale_request (
            request_no,
            order_id,
            order_item_id,
            tenant_id,
            consumer_id,
            quantity,
            requested_amount,
            reason,
            status,
            merchant_remark,
            decided_by,
            decided_at
        )
        VALUES (
            #{requestNo},
            #{orderId},
            #{orderItemId},
            #{tenantId},
            #{consumerId},
            #{quantity},
            #{requestedAmount},
            #{reason},
            #{status},
            #{merchantRemark},
            #{decidedBy},
            #{decidedAt}
        )
        """)
    //新增一条售后记录
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AfterSaleRequest request);

    @Select("""
        SELECT
            id,
            request_no AS requestNo,
            order_id AS orderId,
            order_item_id AS orderItemId,
            tenant_id AS tenantId,
            consumer_id AS consumerId,
            quantity,
            requested_amount AS requestedAmount,
            reason,
            status,
            merchant_remark AS merchantRemark,
            decided_by AS decidedBy,
            decided_at AS decidedAt,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM after_sale_request
        WHERE consumer_id = #{consumerId}
        ORDER BY created_at DESC, id DESC
        """)
    //查询当前消费者自己的售后申请列表
    List<AfterSaleRequest> selectByConsumerId(@Param("consumerId") Long consumerId);

    @Select("""
        SELECT
            id,
            request_no AS requestNo,
            order_id AS orderId,
            order_item_id AS orderItemId,
            tenant_id AS tenantId,
            consumer_id AS consumerId,
            quantity,
            requested_amount AS requestedAmount,
            reason,
            status,
            merchant_remark AS merchantRemark,
            decided_by AS decidedBy,
            decided_at AS decidedAt,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM after_sale_request
        WHERE tenant_id = #{tenantId}
        ORDER BY created_at DESC, id DESC
        """)
    //商家查询自己租户下的售后申请列表。
    List<AfterSaleRequest> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("""
        SELECT
            id,
            request_no AS requestNo,
            order_id AS orderId,
            order_item_id AS orderItemId,
            tenant_id AS tenantId,
            consumer_id AS consumerId,
            quantity,
            requested_amount AS requestedAmount,
            reason,
            status,
            merchant_remark AS merchantRemark,
            decided_by AS decidedBy,
            decided_at AS decidedAt,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM after_sale_request
        WHERE id = #{id}
          AND consumer_id = #{consumerId}
        """)
    //消费者端:消费者查询自己的某条售后详情。!:带 id + consumer_id，防止消费者 A 读取消费者 B 的申请。
    AfterSaleRequest selectByIdAndConsumerId(@Param("id") Long id, @Param("consumerId") Long consumerId );

    @Select("""
        SELECT
            id,
            request_no AS requestNo,
            order_id AS orderId,
            order_item_id AS orderItemId,
            tenant_id AS tenantId,
            consumer_id AS consumerId,
            quantity,
            requested_amount AS requestedAmount,
            reason,
            status,
            merchant_remark AS merchantRemark,
            decided_by AS decidedBy,
            decided_at AS decidedAt,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM after_sale_request
        WHERE id = #{id}
          AND tenant_id = #{tenantId}
        """)
    //商家端:商家查询自己店铺的某条售后详情。!:带 id + tenantId_id,防止商家 A 读取商家 B 的售后
    AfterSaleRequest selectByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId
    );

    @Update("""
        UPDATE after_sale_request
        SET status = #{toStatus},
            merchant_remark = #{merchantRemark},
            decided_by = #{decidedBy},
            decided_at = #{decidedAt}
        WHERE id = #{id}
          AND tenant_id = #{tenantId}
          AND status = #{fromStatus}
        """)
    //商家审核售后申请时，安全地修改状
    int updateStatusByTenantIdAndExpectedStatus(
            @Param("id") Long id,
            @Param("tenantId") Long tenantId,
            @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus,
            @Param("merchantRemark") String merchantRemark,
            @Param("decidedBy") Long decidedBy,
            @Param("decidedAt") LocalDateTime decidedAt
    );
}
