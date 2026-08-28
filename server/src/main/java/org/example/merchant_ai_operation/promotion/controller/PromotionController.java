package org.example.merchant_ai_operation.promotion.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.promotion.dto.CreatePromotionRequest;
import org.example.merchant_ai_operation.promotion.service.PromotionRedisPreheatService;
import org.example.merchant_ai_operation.promotion.service.PromotionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionRedisPreheatService promotionRedisPreheatService;
    public PromotionController(
            PromotionService promotionService,
            PromotionRedisPreheatService promotionRedisPreheatService) {
        this.promotionService = promotionService;
        this.promotionRedisPreheatService = promotionRedisPreheatService;
    }

    //创建促销活动接口
    @PostMapping
    public ApiResponse<Long> createPromotion( @Valid @RequestBody CreatePromotionRequest request) {
        Long activityId = promotionService.createPromotionActivity(request);
        return ApiResponse.ok(activityId);
    }

    //删除促销活动接口
    @DeleteMapping("/{activityId}")
    public ApiResponse<Void> cancelPromotion(@PathVariable Long activityId) {
        promotionService.cancelPromotionActivity(activityId);
        return ApiResponse.ok(null);
    }

    //预热活动接口:将活动商品写进redis里
    @PostMapping("/{activityId}/preheat")
    public ApiResponse<Void> preheatPromotion(@PathVariable Long activityId) {
        promotionRedisPreheatService.preheat(activityId);
        return ApiResponse.ok(null);
    }
}
