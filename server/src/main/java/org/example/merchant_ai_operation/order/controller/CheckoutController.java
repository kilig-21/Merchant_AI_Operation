package org.example.merchant_ai_operation.order.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.order.dto.CreateCheckoutRequest;
import org.example.merchant_ai_operation.order.service.CheckoutGroupService;
import org.example.merchant_ai_operation.order.service.CheckoutService;
import org.example.merchant_ai_operation.order.vo.CheckoutGroupDetailVO;
import org.example.merchant_ai_operation.order.vo.CreateCheckoutGroupVO;
import org.springframework.web.bind.annotation.*;


@RestController
public class CheckoutController {
    private final CheckoutService checkoutService;
    private final CheckoutGroupService checkoutGroupService;
    public CheckoutController(
            CheckoutService checkoutService,
            CheckoutGroupService checkoutGroupService)
    {
        this.checkoutService = checkoutService;
        this.checkoutGroupService = checkoutGroupService;
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

    //返回组订单详情的接口
    @GetMapping("/api/checkouts/{checkoutGroupId}")
    public ApiResponse<CheckoutGroupDetailVO> getMyDetail(@PathVariable Long checkoutGroupId) {
        return ApiResponse.ok(checkoutGroupService.getMyDetail(checkoutGroupId));
    }
}
