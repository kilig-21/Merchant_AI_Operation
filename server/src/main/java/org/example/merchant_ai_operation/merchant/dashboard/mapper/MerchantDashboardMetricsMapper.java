package org.example.merchant_ai_operation.merchant.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.merchant_ai_operation.merchant.dashboard.vo.MerchantDashboardMetricsVO;
import org.example.merchant_ai_operation.merchant.dashboard.vo.MerchantDashboardTrendPointVO;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MerchantDashboardMetricsMapper {


    @Select("""
            SELECT
                (
                    SELECT COUNT(*)
                    FROM commerce_order o
                    WHERE o.tenant_id = #{tenantId}
                      AND o.created_at >= #{startAt}
                      AND o.created_at < #{endAt}
                      AND o.status NOT IN ('CANCELLED', 'CLOSED')
                ) AS validOrderCount,
                COALESCE((
                    SELECT SUM(o.total_amount)
                    FROM commerce_order o
                    WHERE o.tenant_id = #{tenantId}
                      AND o.created_at >= #{startAt}
                      AND o.created_at < #{endAt}
                      AND o.status = 'PAID'
                ), 0) AS paidRevenue,
                (
                    SELECT COUNT(*)
                    FROM commerce_order o
                    WHERE o.tenant_id = #{tenantId}
                      AND o.created_at >= #{startAt}
                      AND o.created_at < #{endAt}
                      AND o.status = 'PENDING_PAYMENT'
                ) AS pendingPaymentCount,
                (
                    SELECT COUNT(DISTINCT s.spu_id)
                    FROM product_sku s
                    JOIN product_spu p ON p.id = s.spu_id
                    WHERE s.tenant_id = #{tenantId}
                      AND p.tenant_id = #{tenantId}
                      AND s.status = 'ON_SALE'
                      AND p.status = 'ON_SALE'
                      AND s.available_stock <= #{lowStockThreshold}
                ) AS lowStockProductCount
            FROM dual
            """)
    //商家端查询指标
    MerchantDashboardMetricsVO selectMetrics(
            @Param("tenantId") Long tenantId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("lowStockThreshold") int lowStockThreshold
    );

    @Select("""
        SELECT
            DATE(o.created_at) AS date,
            COUNT(
                CASE
                    WHEN o.status NOT IN ('CANCELLED', 'CLOSED') THEN 1
                END
            ) AS orderCount,
            COALESCE(SUM(
                CASE
                    WHEN o.status = 'PAID' THEN o.total_amount
                    ELSE 0
                END
            ), 0) AS paidRevenue
        FROM commerce_order o
        WHERE o.tenant_id = #{tenantId}
          AND o.created_at >= #{startAt}
          AND o.created_at < #{endAt}
        GROUP BY DATE(o.created_at)
        ORDER BY DATE(o.created_at) ASC
        """)
    //从数据库按天汇总真实订单数据
    List<MerchantDashboardTrendPointVO> selectDailyTrends(
            @Param("tenantId") Long tenantId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
