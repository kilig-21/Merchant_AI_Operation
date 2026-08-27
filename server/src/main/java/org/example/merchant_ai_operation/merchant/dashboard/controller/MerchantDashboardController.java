package org.example.merchant_ai_operation.merchant.dashboard.controller;


import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.merchant.dashboard.service.MerchantDashboardService;
import org.example.merchant_ai_operation.merchant.dashboard.vo.MerchantDashboardMetricsVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/merchant/dashboard")
public class MerchantDashboardController {
    private final MerchantDashboardService dashboardService;
    public MerchantDashboardController(MerchantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
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
}
