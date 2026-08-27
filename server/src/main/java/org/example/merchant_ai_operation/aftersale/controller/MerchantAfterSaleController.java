package org.example.merchant_ai_operation.aftersale.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.aftersale.dto.ReviewAfterSaleRequest;
import org.example.merchant_ai_operation.aftersale.service.AfterSaleService;
import org.example.merchant_ai_operation.aftersale.vo.AfterSaleRequestVO;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
它面向商家：
GET  /api/merchant/after-sales
GET  /api/merchant/after-sales/{id}
POST /api/merchant/after-sales/{id}/decision
作用是：
- 商家查看自己店铺的售后申请；
- 商家查看售后详情；
- 商家审核售后申请。
*/

@RestController
@RequestMapping("/api/merchant/after-sales")
public class MerchantAfterSaleController {
    private final AfterSaleService afterSaleService;

    public MerchantAfterSaleController(AfterSaleService afterSaleService) {
        this.afterSaleService = afterSaleService;
    }

    @GetMapping
    public ApiResponse<List<AfterSaleRequestVO>> listMine(){
        return ApiResponse.ok(afterSaleService.listMerchantRequests());
    }

    @GetMapping("/{id}")
    public ApiResponse<AfterSaleRequestVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(afterSaleService.getMerchantRequest(id));
    }

    @PostMapping("/{id}/decision")
    public ApiResponse<AfterSaleRequestVO> decision(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAfterSaleRequest request
    ) {
        return ApiResponse.ok(afterSaleService.review(id, request));
    }

}
