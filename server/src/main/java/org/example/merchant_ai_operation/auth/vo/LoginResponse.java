package org.example.merchant_ai_operation.auth.vo;


//登陆成功后返回给用户的字段:
public record LoginResponse(
        String accessToken,
        CurrentUserVO user
) {
}
