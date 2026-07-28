package org.example.merchant_ai_operation.publicapi.product.controller;


import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.publicapi.product.service.PublicProductService;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicProductDetailVO;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicProductListItemVO;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicSkuAvailabilityVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PublicProductController {
    private final PublicProductService publicProductService;
    public PublicProductController(PublicProductService publicProductService) {
        this.publicProductService = publicProductService;
    }


    @GetMapping("/api/public/products/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("public-product-pong");
    }


    @GetMapping("/api/public/stores/{storeId}/products")
    public ApiResponse<List<PublicProductListItemVO>> listStoreProducts(
            //从 URL 里拿店铺 ID，例如 /stores/1001/products，这里的 1001 就会进入 storeId。
            @PathVariable Long storeId,
            //Query 参数可传可不传，例如 ?page=1&size=10。
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ){
        return ApiResponse.ok(publicProductService.listStoreProducts(storeId, page, size));
    }

    //用户查询具体商品:
    @GetMapping("/api/public/products/{spuId}")
    public ApiResponse<PublicProductDetailVO> getProductDetail(@PathVariable Long spuId){
        return ApiResponse.ok(publicProductService.getProductDetail(spuId));
    }

    @GetMapping("/api/public/skus/{skuId}/availability")
    public ApiResponse<PublicSkuAvailabilityVO> getSkuAvailability(@PathVariable Long skuId){
        return ApiResponse.ok(publicProductService.getSkuAvailability(skuId));

    }



}
