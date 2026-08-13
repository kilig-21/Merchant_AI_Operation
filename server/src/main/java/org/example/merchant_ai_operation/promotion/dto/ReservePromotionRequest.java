package org.example.merchant_ai_operation.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservePromotionRequest(
        @NotNull(message = "活动商品不能为空")
        //活动商品记录的数据库 ID。
        Long activityItemId,

        @NotNull(message = "购买数量不能为空")
        @Positive(message = "购买数量必须大于 0")
        Integer quantity,

        @NotBlank(message = "请求幂等键不能为空")
        String requestKey

){

}
