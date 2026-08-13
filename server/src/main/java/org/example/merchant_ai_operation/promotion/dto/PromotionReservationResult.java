package org.example.merchant_ai_operation.promotion.dto;

/*
表示 Lua 脚本返回的业务结果：
code = 1  → 新获得抢购资格
code = 2  → 重复请求，返回原资格
code < 0  → 未预热、未开始、结束、售罄或超限
*/
public record PromotionReservationResult(
        int code,
        String reservationId
) {
}