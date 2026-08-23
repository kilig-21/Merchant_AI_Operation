package org.example.merchant_ai_operation.order.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.order.dto.CreateCheckoutRequest;
import org.example.merchant_ai_operation.order.service.CheckoutService;
import org.example.merchant_ai_operation.order.vo.CreateCheckoutGroupVO;
import org.springframework.web.bind.annotation.*;


@RestController
public class CheckoutController {
    private final CheckoutService checkoutService;
    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    //结算跨商家订单的接口
    @PostMapping("/api/checkouts/prepare")
    public ApiResponse<CreateCheckoutGroupVO> prepare(@Valid @RequestBody CreateCheckoutRequest request) {
        return ApiResponse.ok(checkoutService.createPendingCheckout(request));
    }

    //创建组的子订单的接口
    @PostMapping("/api/checkouts/{checkoutGroupId}/orders")
    public ApiResponse<CreateCheckoutGroupVO> createChildOrders(
            @PathVariable Long checkoutGroupId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateCheckoutRequest request) {

        return ApiResponse.ok(
                checkoutService.createChildOrders(
                        checkoutGroupId,
                        idempotencyKey,
                        request
                )
        );
    }
}
