package org.example.merchant_ai_operation.order.controller;


import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.order.service.OrderService;
import org.example.merchant_ai_operation.order.vo.OrderDetailVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {
    private final OrderService orderService;
    public MerchantOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    //商家端列出订单列表接口
    @GetMapping
    public ApiResponse<List<OrderDetailVO>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ){ return ApiResponse.ok(orderService.listMerchantOrders(page, size));}
 }
