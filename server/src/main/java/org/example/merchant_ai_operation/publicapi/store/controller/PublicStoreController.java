package org.example.merchant_ai_operation.publicapi.store.controller;

import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.publicapi.store.service.PublicStoreService;
import org.example.merchant_ai_operation.publicapi.store.vo.PublicMarketplaceProductVO;
import org.example.merchant_ai_operation.publicapi.store.vo.PublicStoreSummaryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/public/stores")
public class PublicStoreController {
    private final PublicStoreService publicStoreService;

    public PublicStoreController(PublicStoreService publicStoreService) {
        this.publicStoreService = publicStoreService;
    }

    //列出商家目录
    @GetMapping
    public ApiResponse<List<PublicStoreSummaryVO>> listStores() {
        return ApiResponse.ok(publicStoreService.listPublicStores());
    }

    //列出商品信息和来自哪个商家

    @GetMapping("/products/search")
    public ApiResponse<List<PublicMarketplaceProductVO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(
                publicStoreService.searchPublicProducts(
                        keyword,
                        storeId,
                        page,
                        size
                )
        );
    }
}
