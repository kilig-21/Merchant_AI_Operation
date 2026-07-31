package org.example.merchant_ai_operation.order.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.service.OrderService;
import org.example.merchant_ai_operation.order.vo.CreateOrderVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/orders")
    public ApiResponse<CreateOrderVO> createOrder(@Valid @RequestBody CreateOrderRequest request){
        return ApiResponse.ok(orderService.createOrderVO(request));
    }





}
