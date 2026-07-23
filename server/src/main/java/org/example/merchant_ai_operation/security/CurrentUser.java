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
}
