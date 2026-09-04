package org.example.merchant_ai_operation.merchant.dashboard.service;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.dashboard.mapper.MerchantDashboardMetricsMapper;
import org.example.merchant_ai_operation.merchant.dashboard.vo.MerchantDashboardMetricsVO;
import org.example.merchant_ai_operation.merchant.dashboard.vo.MerchantDashboardTrendPointVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MerchantDashboardService {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private final MerchantDashboardMetricsMapper metricsMapper;
    private static final int MAX_TREND_DAYS = 31;

    public MerchantDashboardService(MerchantDashboardMetricsMapper metricsMapper) {
        this.metricsMapper = metricsMapper;
    }

    public MerchantDashboardMetricsVO getMetricsVO(LocalDate startDate, LocalDate endDate) {
        if(startDate == null || endDate == null) {throw new BizException(400,"日期不能为空");}
        if(endDate.isBefore(startDate)) {throw new BizException(400,"结束日期不能早于开始日期");}

        Long tenantId = CurrentUser.requiredMerchantTenantId();

        LocalDateTime startAt = startDate.atStartOfDay();
        LocalDateTime endAt = endDate.plusDays(1).atStartOfDay();

        return metricsMapper.selectMetrics(
                tenantId,
                startAt,
                endAt,
                LOW_STOCK_THRESHOLD
        );
    }

    public List<MerchantDashboardTrendPointVO> getDailyTrends(
            LocalDate startDate,
            LocalDate endDate
    ){
        if (startDate == null || endDate == null) {throw new BizException(400, "日期不能为空");}
        if (endDate.isBefore(startDate)) {throw new BizException(400, "结束日期不能早于开始日期");}

        //先校验天数
        long dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (dayCount > MAX_TREND_DAYS) {throw new BizException(400, "趋势查询最多支持31天");}


        Long tenantId = CurrentUser.requiredMerchantTenantId();
        LocalDateTime startAt = startDate.atStartOfDay();
        LocalDateTime endAt = endDate.plusDays(1).atStartOfDay();

        //Mapper 返回数据库实际有订单的日期
        List<MerchantDashboardTrendPointVO> actualPoints = metricsMapper.selectDailyTrends(tenantId, startAt, endAt);

        //把实际数据按日期放进 Map，便于快速查找。
        Map<LocalDate, MerchantDashboardTrendPointVO> pointsByDate =
                actualPoints.stream().collect(Collectors.toMap(
                        MerchantDashboardTrendPointVO::date,
                        point -> point
                ));

        //生成完整日期范围；没有订单的日期补 0，前端不会画出虚假趋势或断裂日期。
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> pointsByDate.getOrDefault(
                        date,
                        new MerchantDashboardTrendPointVO(date, 0L, BigDecimal.ZERO)
                ))
                .toList();
    }
}
