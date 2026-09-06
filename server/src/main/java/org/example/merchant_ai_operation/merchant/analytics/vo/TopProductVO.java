package org.example.merchant_ai_operation.merchant.analytics.vo;

import java.math.BigDecimal;

//热销商品 VO
public record TopProductVO(
        Long skuId,
        String skuName,
        Long soldQuantity,
        BigDecimal paidRevenue
) {
}