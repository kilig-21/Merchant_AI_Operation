package org.example.merchant_ai_operation.merchant.analytics.vo;

import java.math.BigDecimal;

//创建售后申请率 VO
public record AfterSaleRateVO(
        Long paidOrderItemCount,
        Long afterSaleOrderItemCount,
        BigDecimal afterSaleRate
) {
}