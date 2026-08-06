package org.example.merchant_ai_operation.merchant.product.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

//更改商品价格的要求
public record UpdateSkuPriceRequest(
        @NotNull(message = "销售价格不能为空")
        @DecimalMin(value = "0.01", message = "销售价格必须大于0")
        BigDecimal salePrice
) {
}
