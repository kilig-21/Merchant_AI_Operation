package org.example.merchant_ai_operation.aftersale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/*
* 消费者提交售后申请时使用。
*/
public record SubmitAfterSaleRequest (
        @NotNull(message = "订单项不能为空")
        Long orderItemId,

        @NotNull(message = "售后数量不能为空")
        @Positive(message = "售后数量必须大于 0")
        Integer quantity,

        @NotBlank(message = "售后原因不能为空")
        @Size(max = 255, message = "售后原因不能超过255个字符")
        String reason
){
}
