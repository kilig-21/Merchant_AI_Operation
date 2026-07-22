package org.example.merchant_ai_operation.auth.vo;


import org.example.merchant_ai_operation.user.SysUser;

//记住,几乎这种类都是record的类型!!!
//VO:View Object
public record CurrentUserVO (
        Long id,
        String username,
        String userType,
        Long tenantId
){
    public static CurrentUserVO from (SysUser user){
        return new CurrentUserVO(
                user.getId(),
                user.getUsername(),
                user.getUserType(),
                user.getTenantId()
        );
    }
}
