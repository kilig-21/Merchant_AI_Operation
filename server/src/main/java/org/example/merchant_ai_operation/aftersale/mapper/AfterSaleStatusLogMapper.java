package org.example.merchant_ai_operation.aftersale.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.aftersale.entity.AfterSaleStatusLog;

import java.util.List;

/*
操作日志表：
after_sale_status_log
负责记录状态变化历史，例如：
SUBMITTED → REVIEWING
REVIEWING → APPROVED
通常记录：
- 哪一条售后申请；
- 原状态；
- 新状态；
- 谁操作的；
- 操作者类型；
- 备注；
- 操作时间。
它关注的是：
这条售后申请过去经历过哪些状态变化？
*/

@Mapper
public interface AfterSaleStatusLogMapper {

    @Insert("""
        INSERT INTO after_sale_status_log (
            after_sale_id,
            from_status,
            to_status,
            operator_id,
            operator_type,
            remark
        )
        VALUES (
            #{afterSaleId},
            #{fromStatus},
            #{toStatus},
            #{operatorId},
            #{operatorType},
            #{remark}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AfterSaleStatusLog log);

    @Select("""
        SELECT
            id,
            after_sale_id AS afterSaleId,
            from_status AS fromStatus,
            to_status AS toStatus,
            operator_id AS operatorId,
            operator_type AS operatorType,
            remark,
            created_at AS createdAt
        FROM after_sale_status_log
        WHERE after_sale_id = #{afterSaleId}
        ORDER BY created_at ASC, id ASC
        """)
    List<AfterSaleStatusLog> selectByAfterSaleId(
            @Param("afterSaleId") Long afterSaleId
    );
}

/*
商家审核一条售后申请时，Service 会依次做两件事：
1. AfterSaleRequestMapper
   修改主表状态：REVIEWING → APPROVED

2. AfterSaleStatusLogMapper
   写入日志：记录这次状态变化
*/
