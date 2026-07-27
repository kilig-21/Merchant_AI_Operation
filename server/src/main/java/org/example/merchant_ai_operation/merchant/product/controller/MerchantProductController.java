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

//POST /api/merchant/products                  创建 SPU
//POST /api/merchant/products/{id}/skus        新增 SKU
//POST /api/merchant/products/{id}/publish     上架
//POST /api/merchant/products/{id}/unpublish   下架
//GET  /api/merchant/products                  列表


@RestController
@RequestMapping("/api/merchant/products")
public class MerchantProductController {

    private final ProductService  productService;
    public MerchantProductController(ProductService productService) {
        this.productService = productService;
    }

    //创建商品类型
    @PostMapping
    public ApiResponse<Map<String, Long>> create (@Valid @RequestBody CreateProductRequest request) {

        Long productId = productService.createProductSpu(request);
        return ApiResponse.ok((Map.of("id", productId)));

    }

    //创建商品
    @PostMapping("/{id}/skus")
    public ApiResponse<Map<String, Long>> createSku(@PathVariable Long id,
                                                    @Valid @RequestBody CreateSkuRequest request) {
        Long skuId = productService.createSku(id, request);
        return ApiResponse.ok(Map.of("id", skuId));
    }


    //上架商品
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        productService.publishProduct(id);
        return ApiResponse.ok(null);
    }

    //下架商品
    @PostMapping("/{id}/unpublish")
    public ApiResponse<Void> unpublish(@PathVariable Long id) {
        productService.unpublishProduct(id);
        return ApiResponse.ok(null);
    }


    //列出商品列表
    @GetMapping
    public ApiResponse<List<MerchantProductVO>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(productService.listMerchantProducts(page, size, keyword));
    }




}

