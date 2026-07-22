package org.example.merchant_ai_operation.user;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper//告诉 Spring：这是 MyBatis 的数据库访问接口。
public interface UserMapper {
    //直接把 SQL 写在 Java 方法上。
    //AS 是告诉 MyBatis：查出来的列要塞到 Java 对象的哪个字段里。
    //#{username} 不是字符串拼接，它是 MyBatis 的参数占位，会安全地把方法参数传进去。
    @Select("""
            SELECT
                id,
                tenant_id AS tenantId,
                username,
                password_hash AS passwordHash,
                user_type AS userType,
                status,
                created_at AS createdAt
            FROM sys_user
            WHERE username = #{username} 
            LIMIT 1
            """)
    SysUser selectByUsername(String username);

}
