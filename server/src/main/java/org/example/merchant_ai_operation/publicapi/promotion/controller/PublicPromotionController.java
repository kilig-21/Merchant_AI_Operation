package org.example.merchant_ai_operation.publicapi.promotion.controller;

import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.publicapi.promotion.service.PublicPromotionService;
import org.example.merchant_ai_operation.publicapi.promotion.vo.PublicPromotionActivityDetailVO;
import org.example.merchant_ai_operation.publicapi.promotion.vo.PublicPromotionActivityListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicPromotionController {

    private final PublicPromotionService publicPromotionService;

    public PublicPromotionController(
            PublicPromotionService publicPromotionService
    ) {
        this.publicPromotionService = publicPromotionService;
    }

    @GetMapping("/api/public/promotions")
    // 查询消费者可见的限量促销活动列表
    public ApiResponse<PublicPromotionActivityListVO> listVisibleActivities() {
        return ApiResponse.ok(
                publicPromotionService.listVisibleActivities()
        );
    }

    @GetMapping("/api/public/promotions/{activityId}")
    // 查询指定的消费者可见限量促销活动详情。
    public ApiResponse<PublicPromotionActivityDetailVO> getVisibleActivity(
            @PathVariable Long activityId
    ) {
        return ApiResponse.ok(
                publicPromotionService.getVisibleActivity(activityId)
        );
    }
}