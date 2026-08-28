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

    //返回组订单详情的接口
    @GetMapping("/api/checkouts/{checkoutGroupId}")
    public ApiResponse<CheckoutGroupDetailVO> getMyDetail(@PathVariable Long checkoutGroupId) {
        return ApiResponse.ok(checkoutGroupService.getMyDetail(checkoutGroupId));
    }

    //模拟组消费接口
    @PostMapping("/api/checkouts/{checkoutGroupId}/mock-pay")
    public ApiResponse<Void> mockPayCheckoutGroup(@PathVariable Long checkoutGroupId) {
        checkoutService.mockPayCheckoutGroup(checkoutGroupId);
        return ApiResponse.ok(null);
    }

    //取消组订单接口
    @PostMapping("/api/checkouts/{checkoutGroupId}/cancel")
    public ApiResponse<Void> cancelCheckoutGroup(@PathVariable Long checkoutGroupId) {
        checkoutService.cancelCheckoutGroup(checkoutGroupId);
        return ApiResponse.ok(null);
    }


    //结算组订单的"一键原子提交!"
    @PostMapping("/api/checkouts")
    public ApiResponse<CreateCheckoutGroupVO> submitCheckout(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateCheckoutRequest request
    ) {return ApiResponse.ok(checkoutService.submitCheckout(idempotencyKey, request));}

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
