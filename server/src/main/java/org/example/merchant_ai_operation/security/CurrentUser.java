package org.example.merchant_ai_operation.security;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;


public final class CurrentUser {

    private CurrentUser() {}

    public static LoginPrincipal required() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof LoginPrincipal principal)) {
            throw new AccessDeniedException("未登录");
        }

        return principal;
    }

    public static  Long requiredMerchantTenantId(){
        LoginPrincipal principal=required();

        if (principal.tenantId()==null
                ||principal.userType() == null
                ||!principal.userType().startsWith("MERCHANT_")) {
            throw new AccessDeniedException("不是商家账号");

        }

        //返回商家的Id
        return principal.tenantId();
    }

}
