package org.example.merchant_ai_operation.merchant.dashboard.controller;


import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.merchant.dashboard.service.MerchantDashboardService;
import org.example.merchant_ai_operation.merchant.dashboard.vo.MerchantDashboardMetricsVO;
import org.example.merchant_ai_operation.merchant.dashboard.vo.MerchantDashboardTrendPointVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/dashboard")
public class MerchantDashboardController {
    private final MerchantDashboardService dashboardService;
    public MerchantDashboardController(MerchantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    //这段时间的总览卡片接口 -> 供页面显示四张经营卡片
    @GetMapping("/metrics")
    public ApiResponse<MerchantDashboardMetricsVO> metrics(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ApiResponse.ok(
                dashboardService.getMetricsVO(startDate, endDate)
        );
    }

    //这段时间每天怎样变化接口 -> 供折线图使用
    @GetMapping("/trends")
    public ApiResponse<List<MerchantDashboardTrendPointVO>> trends(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ApiResponse.ok(
                dashboardService.getDailyTrends(startDate, endDate)
        );
    }
}
