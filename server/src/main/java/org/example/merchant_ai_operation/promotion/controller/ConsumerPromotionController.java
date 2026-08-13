package org.example.merchant_ai_operation.promotion.controller;

import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.promotion.dto.PromotionReservationResult;
import org.example.merchant_ai_operation.promotion.dto.ReservePromotionRequest;
import org.example.merchant_ai_operation.promotion.service.PromotionReservationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
public class ConsumerPromotionController {

    private final PromotionReservationService promotionReservationService;

    public ConsumerPromotionController(
            PromotionReservationService promotionReservationService
    ) {
        this.promotionReservationService = promotionReservationService;
    }

    @PostMapping("/reservations")
    public ApiResponse<PromotionReservationResult> reserve(
            @Valid @RequestBody ReservePromotionRequest request
    ) {
        return ApiResponse.ok(promotionReservationService.reserve(request));
    }
}