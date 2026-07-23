package org.example.merchant_ai_operation.security;


//它表示“已经从 token 里解析出来的登录人”。
public record LoginPrincipal(
        Long userId,
        Long tenantId,
        String userType
){


}
