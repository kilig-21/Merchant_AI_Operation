package org.example.merchant_ai_operation.promotion.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.promotion.dto.CreatePromotionRequest;
import org.example.merchant_ai_operation.promotion.service.PromotionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    public ApiResponse<Long> createPromotion( @Valid @RequestBody CreatePromotionRequest request) {
        Long activityId = promotionService.createPromotionActivity(request);
        return ApiResponse.ok(activityId);
    }

    @DeleteMapping("/{activityId}")
    public ApiResponse<Void> cancelPromotion(@PathVariable Long activityId) {
        promotionService.cancelPromotionActivity(activityId);
        return ApiResponse.ok(null);
    }
}
