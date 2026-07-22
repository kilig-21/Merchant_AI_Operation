package org.example.merchant_ai_operation.auth.dto;


import jakarta.validation.constraints.NotBlank;



public record LoginRequest(
        //@NotBlank表示这个字段不能是 null、空字符串、纯空格
        //message:是参数错误时返回给前端看的提示。
        @NotBlank(message = "用户名不能为空")
        String username,


        @NotBlank(message = "密码不能为空")
        String password

){


}