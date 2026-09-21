package org.example.merchant_ai_operation.merchant.analytics.controller;

import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.merchant.analytics.service.MerchantAnalyticsQueryService;
import org.example.merchant_ai_operation.merchant.analytics.vo.AfterSaleRateVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.MerchantOperatingSummaryVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.PromotionPerformanceVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.TopProductVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/analytics")
public class MerchantAnalyticsController {

    private final MerchantAnalyticsQueryService analyticsQueryService;
    public MerchantAnalyticsController(MerchantAnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }


    /**
     * 查询当前商家的经营汇总。
     */
    @GetMapping("/summary")
    public ApiResponse<MerchantOperatingSummaryVO> summary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ApiResponse.ok(
                analyticsQueryService.getOperatingSummary(startDate, endDate)
        );
    }

    /**
     * 查询当前商家的热销 SKU。
     */
    @GetMapping("/top-products")
    public ApiResponse<List<TopProductVO>> topProducts(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "5")
            Integer limit
    ) {
        return ApiResponse.ok(
                analyticsQueryService.getTopProducts(
                        startDate,
                        endDate,
                        limit
                )
        );
    }

    /**
     * 查询当前商家的促销活动表现。
     */
    @GetMapping("/promotions")
    public ApiResponse<List<PromotionPerformanceVO>> promotions(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "5")
            Integer limit
    ) {
        return ApiResponse.ok(
                analyticsQueryService.getPromotionPerformance(
                        startDate,
                        endDate,
                        limit
                )
        );
    }

    /**
     * 查询当前商家的售后申请率。
     */
    @GetMapping("/after-sale-rate")
    public ApiResponse<AfterSaleRateVO> afterSaleRate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ApiResponse.ok(
                analyticsQueryService.getAfterSaleRate(startDate, endDate)
        );
    }

}