package org.example.merchant_ai_operation.merchant.analytics.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.analytics.mapper.MerchantAnalyticsMapper;
import org.example.merchant_ai_operation.merchant.analytics.vo.AfterSaleRateVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.MerchantOperatingSummaryVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.PromotionPerformanceVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.TopProductVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MerchantAnalyticsQueryService {
    private static final int MAX_QUERY_DAYS = 31;
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int MIN_LIST_LIMIT = 1;
    private static final int MAX_LIST_LIMIT = 10;

    //内部record
    private record QueryRange(LocalDateTime startAt, LocalDateTime endAt){}

    private final MerchantAnalyticsMapper analyticsMapper;
    public MerchantAnalyticsQueryService(MerchantAnalyticsMapper analyticsMapper) {
        this.analyticsMapper = analyticsMapper;
    }

    /**
     * 查询当前商家在指定自然日范围内的经营汇总。
     *
     * <p>该方法会校验日期范围，从安全上下文获取当前商家的 tenantId，
     * 查询订单与库存的原始聚合数据，并计算客单价后组装最终 VO。</p>
     *
     * @param startDate 开始日期，包含当天
     * @param endDate   结束日期，包含当天
     * @return 当前商家的经营汇总，包括订单数、营业额、客单价和低库存商品数
     * @throws BizException 日期为空、日期倒序或查询范围超过 31 天时抛出
     */
    public MerchantOperatingSummaryVO getOperatingSummary(LocalDate startDate,LocalDate endDate){
        //先校验时间
        QueryRange range = normalizeDateRange(startDate,endDate);

        //再获取身份是否对的
        Long tenantId = CurrentUser.requiredMerchantTenantId();

        //先拿内部record -> 后步计算客单价
        MerchantAnalyticsMapper.OperatingSummaryRow row =
                analyticsMapper.selectOperatingSummary(
                        tenantId,
                        range.startAt(),
                        range.endAt(),
                        LOW_STOCK_THRESHOLD
                );

        //计算客单价
        BigDecimal averageOrderValue;
        if (row.paidOrderCount() == 0L) {
            averageOrderValue = BigDecimal.ZERO.setScale(2);
        } else {
            averageOrderValue = row.paidRevenue().divide(
                    BigDecimal.valueOf(row.paidOrderCount()),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        //组装最终经营汇总 VO，交给 Controller 返回
        return new MerchantOperatingSummaryVO(
                row.validOrderCount(),
                row.paidOrderCount(),
                row.paidRevenue(),
                averageOrderValue,              //组装好的客单价
                row.pendingPaymentCount(),
                row.lowStockProductCount()
        );

    }

    /**
     * 查询当前商家的热销 SKU。
     */
    public List<TopProductVO> getTopProducts(
            LocalDate startDate,
            LocalDate endDate,
            Integer limit
    ) {
        QueryRange range = normalizeDateRange(startDate, endDate);
        int safeLimit = validateLimit(limit);

        Long tenantId = CurrentUser.requiredMerchantTenantId();

        return analyticsMapper.selectTopProducts(
                tenantId,
                range.startAt(),
                range.endAt(),
                safeLimit
        );
    }

    /**
     * 查询当前商家的促销活动表现。
     *
     * @param startDate 开始日期，包含当天
     * @param endDate   结束日期，包含当天
     * @param limit     返回活动数量，必须在 1 到 10 之间
     * @return 按促销金额降序排列的活动表现列表
     */
    public List<PromotionPerformanceVO> getPromotionPerformance(
            LocalDate startDate,
            LocalDate endDate,
            Integer limit
    ) {
        QueryRange range = normalizeDateRange(startDate, endDate);
        int safeLimit = validateLimit(limit);

        Long tenantId = CurrentUser.requiredMerchantTenantId();

        List<MerchantAnalyticsMapper.PromotionPerformanceRow> rows =
                analyticsMapper.selectPromotionPerformance(
                        tenantId,
                        range.startAt(),
                        range.endAt(),
                        safeLimit
                );

        return rows.stream()
                .map(row -> new PromotionPerformanceVO(
                        row.activityId(),
                        row.activityName(),
                        row.reservationCount(),
                        row.orderCreatedCount(),
                        row.successfulQuantity(),
                        row.promotionRevenue(),
                        calculateRate(
                                row.orderCreatedCount(),
                                row.reservationCount()
                        )
                ))
                .toList();
    }

    /**
     * 查询当前商家指定日期范围内的售后申请率。
     *
     * @param startDate 开始日期，包含当天
     * @param endDate   结束日期，包含当天
     * @return 已支付订单明细数、发起售后的明细数和售后申请率
     */
    public AfterSaleRateVO getAfterSaleRate(
            LocalDate startDate,
            LocalDate endDate
    ) {
        QueryRange range = normalizeDateRange(startDate, endDate);

        Long tenantId = CurrentUser.requiredMerchantTenantId();

        MerchantAnalyticsMapper.AfterSaleRateRow row =
                analyticsMapper.selectAfterSaleRate(
                        tenantId,
                        range.startAt(),
                        range.endAt()
                );

        BigDecimal afterSaleRate = calculateRate(
                row.afterSaleOrderItemCount(),
                row.paidOrderItemCount()
        );

        return new AfterSaleRateVO(
                row.paidOrderItemCount(),
                row.afterSaleOrderItemCount(),
                afterSaleRate
        );
    }


    //<---------------------------私有方法----------------------------->


    /**
     * 校验并转换日期范围
     */
    private QueryRange normalizeDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {throw new BizException(400, "日期不能为空");}
        if (endDate.isBefore(startDate)) {throw new BizException(400, "结束日期不能早于开始日期");}

        long dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (dayCount > MAX_QUERY_DAYS) {
            throw new BizException(400, "经营查询最多支持31天");
        }

        return new QueryRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );
    }

    /**
     * 校验列表查询的返回数量。
     */
    private int validateLimit(Integer limit) {
        if (limit == null) {
            throw new BizException(400, "返回数量不能为空");
        }

        if (limit < MIN_LIST_LIMIT || limit > MAX_LIST_LIMIT) {
            throw new BizException(400, "返回数量必须在1到10之间");
        }

        return limit;
    }

    /**
     * 计算 0 到 1 之间的业务比率。
     */
    private BigDecimal calculateRate(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0L) {
            return BigDecimal.ZERO.setScale(2);
        }

        if (numerator == null) {
            numerator = 0L;
        }

        return BigDecimal.valueOf(numerator).divide(
                BigDecimal.valueOf(denominator),
                2,
                RoundingMode.HALF_UP
        );
    }

}
