package org.example.merchant_ai_operation.order.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.service.OrderService;
import org.example.merchant_ai_operation.order.vo.CreateOrderVO;
import org.example.merchant_ai_operation.order.vo.OrderDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/orders")
    public ApiResponse<CreateOrderVO> createOrder(
            //如果前端完全不传这个 Header，请求可能在进入 Service 前就被 Spring 拦住 -> 抛出兜底异常
            @RequestHeader(value = "Idempotency-Key",required = false )  String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request){
        return ApiResponse.ok(orderService.createOrderVO(idempotencyKey, request));
    }

    @PostMapping("/api/orders/{id}/mock-pay")
    public ApiResponse<Void> mockPay(@PathVariable("id") Long id) {
        orderService.mockPay(id);
        return ApiResponse.ok(null);
    }

    //展示出所有订单的列表
    @GetMapping("/api/orders")
    public ApiResponse<List<OrderDetailVO>> listMyOrders(){
        return  ApiResponse.ok(orderService.listMyOrders());
    }

    //列出订单的详情
    @GetMapping("/api/orders/{id}")
    public ApiResponse<OrderDetailVO>  getOrderDetail(@PathVariable("id") Long id){
        return ApiResponse.ok(orderService.getMyOrderDetail(id));
    }




}
