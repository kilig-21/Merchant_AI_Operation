package org.example.merchant_ai_operation.controller;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;



//测试参数异常的DTO
public record DebugValidateRequest(
        @NotBlank(message="名称不能为空")//表示字符串不能为空、不能全是空格。
        String name,

        @Min(value=1,message="数量必须大于等于1")//表示数字最小是 1
        Integer quantity
){

}
