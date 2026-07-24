package org.example.merchant_ai_operation.merchant.controller;


import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//商家上下文接口
@RestController
@RequestMapping("/api/merchant")
public class MerchantContextController {

    @GetMapping("/context")
    public ApiResponse<Map<String, Object>> context() {
        LoginPrincipal principal = CurrentUser.required();
        Long tenantId = CurrentUser.requiredMerchantTenantId();

        return ApiResponse.ok(Map.of(
                "userId", principal.userId(),
                "tenantId", tenantId,
                "userType", principal.userType()
        ));

    }


}
