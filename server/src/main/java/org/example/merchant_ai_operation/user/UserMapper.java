package org.example.merchant_ai_operation.user;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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


    //根据id查用户:
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
        WHERE id = #{id}
        LIMIT 1
        """)
    SysUser selectById(Long id);

    //添加消费者:所以商家id写死为null;
    //用户类型就写死为消费者;
    @Insert("""
        INSERT INTO sys_user (
            id,
            tenant_id,
            username,
            password_hash,
            user_type,
            status
        )
        VALUES (
            #{id},
            NULL,
            #{username},
            #{passwordHash},
            'CONSUMER',
            1
        )
    """)
    int insertConsumer(@Param("id") Long id,
                       @Param("username") String username,
                       @Param("passwordHash") String passwordHash);

}
