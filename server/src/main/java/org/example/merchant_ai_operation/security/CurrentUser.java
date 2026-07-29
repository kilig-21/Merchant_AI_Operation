package org.example.merchant_ai_operation.security;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;


public final class CurrentUser {

    private CurrentUser() {}

    // //required() 只保证“已登录”。
    public static LoginPrincipal required() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof LoginPrincipal principal)) {
            throw new AccessDeniedException("未登录");
        }

        return principal;
    }

    //requiredMerchantTenantId() 保证“是商家”，之前商家商品管理用它。
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

    //requiredConsumerId() 保证“是消费者”，购物车、下单、我的订单都会用它。
    public static Long requiredConsumerId(){
        //拿到全局登录信息;
        LoginPrincipal principal=required();

        if (principal.userType() == null || !"CONSUMER".equals(principal.userType())) {
            throw new AccessDeniedException("不是消费者账号");
        }
        return principal.userId();
    }


}
