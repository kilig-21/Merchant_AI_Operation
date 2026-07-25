package org.example.merchant_ai_operation.merchant.product.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.merchant.product.dto.CreateProductRequest;
import org.example.merchant_ai_operation.merchant.product.dto.CreateSkuRequest;
import org.example.merchant_ai_operation.merchant.product.service.ProductService;
import org.example.merchant_ai_operation.merchant.product.vo.MerchantProductVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/products")
public class MerchantProductController {

    private final ProductService  productService;
    public MerchantProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> create (@Valid @RequestBody CreateProductRequest request) {

        Long productId = productService.createProductSpu(request);
        return ApiResponse.ok((Map.of("id", productId)));

    }

    @PostMapping("/{id}/skus")
    public ApiResponse<Map<String, Long>> createSku(@PathVariable Long id,
                                                    @Valid @RequestBody CreateSkuRequest request) {
        Long skuId = productService.createSku(id, request);
        return ApiResponse.ok(Map.of("id", skuId));
    }
    @GetMapping
    public ApiResponse<List<MerchantProductVO>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(productService.listMerchantProducts(page, size, keyword));
    }




}

