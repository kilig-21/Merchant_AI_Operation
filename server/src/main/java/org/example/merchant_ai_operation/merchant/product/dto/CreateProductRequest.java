package org.example.merchant_ai_operation.merchant.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateProductRequest(
        // @NotBlank 能拦住 null、"" 和 "   "
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 128, message = "商品名称不能超过128个字符")
        String name,

        @Size(max = 1000, message = "商品描述不能超过1000个字符")
        String description
) {
}