package org.example.merchant_ai_operation.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//也可以直接import lombok.*引入全部!!
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysUser {
    private Long id;
    private Long tenantId;
    private String username;
    private String passwordHash;
    private String userType;
    private Integer status;
    private LocalDateTime createdAt;

}
