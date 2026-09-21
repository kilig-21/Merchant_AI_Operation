package org.example.merchant_ai_operation.merchant.analytics.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.merchant_ai_operation.merchant.analytics.vo.TopProductVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MerchantAnalyticsMapper {
    /**
     * 承接经营汇总 SQL 返回的原始聚合结果。
     *
     * <p>该对象仅用于 Mapper 与 Service 之间的数据传递。
     * 它提供客单价计算所需的已支付订单数和已支付营业额，
     * 不作为 Controller 的最终接口响应。</p>
     *
     * @param validOrderCount      有效订单数，排除 CANCELLED 和 CLOSED
     * @param paidOrderCount       当前状态为 PAID 的订单数
     * @param paidRevenue          当前状态为 PAID 的订单金额合计
     * @param pendingPaymentCount  当前状态为 PENDING_PAYMENT 的订单数
     * @param lowStockProductCount 当前商家的低库存 SPU 数量
     */
    record OperatingSummaryRow(
            Long validOrderCount,
            Long paidOrderCount,
            BigDecimal paidRevenue,
            Long pendingPaymentCount,
            Long lowStockProductCount
    ) {}

    /**
     * 承接促销表现 SQL 返回的原始活动聚合结果。
     *
     * <p>该对象不包含转化率。Service 会使用成功创建订单的资格数
     * 除以全部资格申请数，处理除零和精度后，再组装最终 VO。</p>
     *
     * @param activityId         促销活动 ID
     * @param activityName       促销活动名称
     * @param reservationCount   活动收到的资格申请总数
     * @param orderCreatedCount  当前状态为 ORDER_CREATED 的资格数
     * @param successfulQuantity 成功创建订单的促销商品件数
     * @param promotionRevenue   成功创建订单的资格对应金额，不代表已支付营业额
     */
    record PromotionPerformanceRow(
            Long activityId,
            String activityName,
            Long reservationCount,
            Long orderCreatedCount,
            Long successfulQuantity,
            BigDecimal promotionRevenue
    ){}

    /**
     * 承接售后申请率计算所需的原始计数。
     *
     * <p>Service 会使用发起过售后的订单明细数除以
     * 已支付订单明细总数，得到最终售后申请率。</p>
     *
     * @param paidOrderItemCount      已支付订单包含的不同订单明细数
     * @param afterSaleOrderItemCount 其中至少发起过一次售后的不同订单明细数
     */
    record AfterSaleRateRow(
            Long paidOrderItemCount,
            Long afterSaleOrderItemCount
    ) {
    }

    /**
     * 根据商家租户和指定时间范围，查询经营汇总所需的原始数据。
     *
     * <p>订单类指标使用左闭右开的时间范围；
     * 低库存商品数是当前库存快照，不受订单日期范围影响。
     * 本方法不计算客单价，客单价由 Service 根据返回结果计算。</p>
     *
     * @param tenantId         当前商家的租户 ID，必须来自服务端安全上下文
     * @param startAt          查询开始时间，包含该时刻
     * @param endAt            查询结束时间，不包含该时刻
     * @param lowStockThreshold 低库存阈值
     * @return 经营汇总所需的五项原始聚合数据
     */
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
            (
                SELECT COUNT(*)
                FROM commerce_order o
                WHERE o.tenant_id = #{tenantId}
                  AND o.created_at >= #{startAt}
                  AND o.created_at < #{endAt}
                  AND o.status = 'PAID'
            ) AS paidOrderCount,
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
    OperatingSummaryRow selectOperatingSummary(
            @Param("tenantId") Long tenantId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("lowStockThreshold") int lowStockThreshold
    );

    /**
     * 查询当前商家指定日期范围内的热销 SKU。
     *
     * <p>只统计当前状态为 PAID 的订单，按销量、销售额和 SKU ID
     * 依次排序，并由 limit 限制返回数量。</p>
     */
    @Select("""
        SELECT
            oi.sku_id AS skuId,
            COALESCE(s.sku_name, MAX(oi.sku_name_snapshot)) AS skuName,
            SUM(oi.quantity) AS soldQuantity,
            COALESCE(SUM(oi.sale_price * oi.quantity), 0) AS paidRevenue
        FROM commerce_order o
        JOIN commerce_order_item oi ON oi.order_id = o.id
        LEFT JOIN product_sku s
               ON s.id = oi.sku_id
              AND s.tenant_id = o.tenant_id
        WHERE o.tenant_id = #{tenantId}
          AND o.created_at >= #{startAt}
          AND o.created_at < #{endAt}
          AND o.status = 'PAID'
        GROUP BY oi.sku_id, s.sku_name
        ORDER BY soldQuantity DESC,
                 paidRevenue DESC,
                 oi.sku_id ASC
        LIMIT #{limit}
        """)
    List<TopProductVO> selectTopProducts(
            @Param("tenantId") Long tenantId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("limit") int limit
    );

    /**
     * 查询当前商家指定日期范围内的促销活动表现。
     *
     * <p>日期按促销资格的创建时间统计，促销金额表示成功创建订单的
     * 资格对应金额，不代表订单已经支付。</p>
     */
    @Select("""
        SELECT
            a.id AS activityId,
            a.name AS activityName,
            COUNT(r.id) AS reservationCount,
            COALESCE(SUM(
                CASE
                    WHEN r.status = 'ORDER_CREATED' THEN 1
                    ELSE 0
                END
            ), 0) AS orderCreatedCount,
            COALESCE(SUM(
                CASE
                    WHEN r.status = 'ORDER_CREATED' THEN r.quantity
                    ELSE 0
                END
            ), 0) AS successfulQuantity,
            COALESCE(SUM(
                CASE
                    WHEN r.status = 'ORDER_CREATED'
                    THEN r.unit_price_snapshot * r.quantity
                    ELSE 0
                END
            ), 0) AS promotionRevenue
        FROM promotion_reservations r
        JOIN promotion_activities a
          ON a.id = r.activity_id
         AND a.tenant_id = r.tenant_id
        WHERE r.tenant_id = #{tenantId}
          AND r.created_at >= #{startAt}
          AND r.created_at < #{endAt}
        GROUP BY a.id, a.name
        ORDER BY promotionRevenue DESC,
                 a.id ASC
        LIMIT #{limit}
        """)
    List<PromotionPerformanceRow> selectPromotionPerformance(
            @Param("tenantId") Long tenantId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("limit") int limit
    );

    /**
     * 查询当前商家指定日期范围内的售后申请率原始计数。
     *
     * <p>日期按已支付订单的创建时间统计。同一个订单明细即使存在
     * 多条售后记录，也只计入一次。</p>
     */
    @Select("""
        SELECT
            COUNT(DISTINCT oi.id) AS paidOrderItemCount,
            COUNT(DISTINCT ar.order_item_id) AS afterSaleOrderItemCount
        FROM commerce_order o
        JOIN commerce_order_item oi
          ON oi.order_id = o.id
        LEFT JOIN after_sale_request ar
          ON ar.order_id = o.id
         AND ar.order_item_id = oi.id
         AND ar.tenant_id = o.tenant_id
        WHERE o.tenant_id = #{tenantId}
          AND o.created_at >= #{startAt}
          AND o.created_at < #{endAt}
          AND o.status = 'PAID'
        """)
    AfterSaleRateRow selectAfterSaleRate(
            @Param("tenantId") Long tenantId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

}