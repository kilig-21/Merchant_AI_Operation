package org.example.merchant_ai_operation.merchant.product.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateSkuRequest(

        @NotBlank(message = "SKU名称不能为空")
        String skuName,

        @NotNull(message = "销售价格不能为空")
        @DecimalMin(value = "0.01", message = "销售价格必须大于0")
        BigDecimal salePrice,

        @NotNull(message = "可售库存不能为空")
        @Min(value = 0, message = "可售库存不能小于0")
        Integer availableStock
) {
}