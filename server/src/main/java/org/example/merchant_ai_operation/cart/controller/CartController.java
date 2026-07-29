package org.example.merchant_ai_operation.cart.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.cart.dto.AddCartItemRequest;
import org.example.merchant_ai_operation.cart.dto.UpdateCartItemRequest;
import org.example.merchant_ai_operation.cart.service.CartService;
import org.example.merchant_ai_operation.cart.vo.CartItemVO;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart/items")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    //增加购物车商品
    @PostMapping
    public ApiResponse<CartItemVO> addItem(@Valid @RequestBody AddCartItemRequest request){
        return ApiResponse.ok(cartService.addItem(request));
    }

    //列出购物车列表
    @GetMapping
    public ApiResponse<List<CartItemVO>> listItems() {
        return ApiResponse.ok(cartService.listItems());
    }

    //根据id增加商品
    @PutMapping("/{id}")
    public ApiResponse<CartItemVO> updateQuantity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ApiResponse.ok(cartService.updateQuantity(id, request));
    }

    //根据id删除商品
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        cartService.deleteItem(id);
        return ApiResponse.ok(null);
    }
}
