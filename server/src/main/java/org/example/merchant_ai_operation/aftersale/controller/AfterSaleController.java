package org.example.merchant_ai_operation.aftersale.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.aftersale.dto.SubmitAfterSaleRequest;
import org.example.merchant_ai_operation.aftersale.service.AfterSaleService;
import org.example.merchant_ai_operation.aftersale.vo.AfterSaleOrderItemContext;
import org.example.merchant_ai_operation.aftersale.vo.AfterSaleRequestVO;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/*
它面向消费者：
POST /api/after-sales
GET  /api/after-sales
GET  /api/after-sales/{id}
作用是：
- 消费者提交售后申请；
- 消费者查看自己的售后列表；
- 消费者查看自己的售后详情。
*/

@RestController
@RequestMapping("/api/after-sales")
public class AfterSaleController {
    private final AfterSaleService afterSaleService;

    public AfterSaleController(AfterSaleService afterSaleService) {
        this.afterSaleService = afterSaleService;
    }

    //提交售后记录接口
    @PostMapping
    public ApiResponse<AfterSaleRequestVO> submit(@Valid @RequestBody SubmitAfterSaleRequest request) {
        return ApiResponse.ok(afterSaleService.submit(request));
    }

    //列出我的所有售后记录的接口
    @GetMapping
    public ApiResponse<List<AfterSaleRequestVO>> listMine() {
        return ApiResponse.ok(afterSaleService.listMyRequest());
    }

    //查看售后记录细节的接口
    @GetMapping("/{id}")
    public ApiResponse<AfterSaleRequestVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(afterSaleService.getMyRequest(id));
    }

    //查询消费者哪些已支付订单项可以申请售后的接口
    @GetMapping("/eligible-orders")
    public ApiResponse<List<AfterSaleOrderItemContext>> eligibleOrders() {
        return ApiResponse.ok(afterSaleService.listEligibleOrderItems());
    }
}
