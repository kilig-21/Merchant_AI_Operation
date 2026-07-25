package org.example.merchant_ai_operation.merchant.product.vo;


import java.time.LocalDateTime;

//返回给前端的
public record MerchantProductVO(

        Long id,                    // 商品ID
        String name,                // 商品名称
        String description,         // 商品描述
        String status,              // 商品状态
        LocalDateTime createdAt,    // 创建时间
        LocalDateTime updatedAt     // 更新时间
){
}
