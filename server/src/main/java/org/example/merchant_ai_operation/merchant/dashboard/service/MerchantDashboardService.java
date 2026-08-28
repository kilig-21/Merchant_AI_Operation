package org.example.merchant_ai_operation.merchant.dashboard.service;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.dashboard.mapper.MerchantDashboardMetricsMapper;
import org.example.merchant_ai_operation.merchant.dashboard.vo.MerchantDashboardMetricsVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class MerchantDashboardService {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private final MerchantDashboardMetricsMapper metricsMapper;

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
}
