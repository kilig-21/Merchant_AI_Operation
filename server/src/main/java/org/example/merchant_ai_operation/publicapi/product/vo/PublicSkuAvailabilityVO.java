package org.example.merchant_ai_operation.publicapi.product.vo;


//主要是返回给前端 选择规格、加入购物车、下单前校验用
public record PublicSkuAvailabilityVO(
        Long skuId,                 //当前查询的是哪个 SKU。
        Boolean purchasable,        //是否可购买。
        Integer availableStock,     //当前可售库存。
        String message              //给前端展示或调试，比如 可购买、库存不足、商品不存在或已下架。
) {
}
