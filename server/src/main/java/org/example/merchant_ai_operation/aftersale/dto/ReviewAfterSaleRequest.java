package org.example.merchant_ai_operation.aftersale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


/*
* 商家审核售后申请时使用。
*/
public record ReviewAfterSaleRequest (
        @NotBlank(message = "审核结果不能为空")
        @Pattern(regexp = "APPROVED|REJECTED", message = "审核结果只能是 APPROVED 或 REJECTED")
        String decision,

        @Size(max = 500, message = "审核备注不能超过500个字符")
        String remark
){
}
