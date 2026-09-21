package org.example.merchant_ai_operation.promotion.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.promotion.dto.PromotionReservationResult;
import org.example.merchant_ai_operation.promotion.dto.ReservePromotionRequest;
import org.example.merchant_ai_operation.promotion.service.PromotionReservationService;
import org.example.merchant_ai_operation.promotion.vo.PromotionReservationDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class ConsumerPromotionController {

    private final PromotionReservationService promotionReservationService;

    public ConsumerPromotionController(
            PromotionReservationService promotionReservationService
    ) {
        this.promotionReservationService = promotionReservationService;
    }

    //保存抢购成功资格接口
    @PostMapping("/reservations")
    public ApiResponse<PromotionReservationResult> reserve(
            @Valid @RequestBody ReservePromotionRequest request
    ) {
        return ApiResponse.ok(promotionReservationService.reserve(request));
    }

    //查询促销抢购资格:
    @GetMapping("/reservations/{reservationId}")
    public ApiResponse<PromotionReservationDetailVO> getReservationDetail(
            @PathVariable("reservationId") String reservationId)
    {
        return ApiResponse.ok(promotionReservationService
                .getReservationDetail(reservationId)
        );
    }

    // 查询当前消费者在指定活动中的抢购资格及异步订单结果。
    @GetMapping("/reservations")
    public ApiResponse<List<PromotionReservationDetailVO>> listMyReservationDetails(
            @RequestParam
            @Positive(message = "活动ID必须大于0")
            Long activityId
    ) {return ApiResponse.ok(promotionReservationService.listMyReservationDetails(activityId));}
}