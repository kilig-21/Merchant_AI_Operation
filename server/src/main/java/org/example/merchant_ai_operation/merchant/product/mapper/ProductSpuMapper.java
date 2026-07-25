package org.example.merchant_ai_operation.merchant.product.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSpu;
import org.example.merchant_ai_operation.merchant.product.vo.MerchantProductVO;

import java.util.List;

@Mapper
public interface ProductSpuMapper {

    @Insert("""
            INSERT INTO product_spu (
                id,
                tenant_id,
                name,
                description,
                status
            )
            VALUES (
                #{spu.id},
                #{spu.tenantId},
                #{spu.name},
                #{spu.description},
                #{spu.status}
            )
            """)
    int insert(@Param("spu") ProductSpu spu);


    @Select("""
            SELECT COUNT(1)
            FROM product_spu
            WHERE id = #{spuId}
            AND tenant_id = #{tenantId}
            """)
    //这个方法的作用是：确认“这个商品 SPU 是否属于当前商家”。
  int countByIdAndTenantId(@Param("spuId") Long spuId,
                         @Param("tenantId") Long tenantId);


    //WHERE tenant_id = #{tenantId}
    //商家商品列表必须只查当前商家
    //keyword 只是搜索条件，没传就查全部。
    //LIMIT/OFFSET 是最基础分页：limit 表示一页多少条，offset 表示跳过多少条。
    @Select("""
        SELECT
            id,
            name,
            description,
            status,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM product_spu
        WHERE tenant_id = #{tenantId}
          AND (
              #{keyword} IS NULL
              OR #{keyword} = ''
              OR name LIKE CONCAT('%', #{keyword}, '%')
          )
        ORDER BY created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<MerchantProductVO> selectMerchantProducts(@Param("tenantId") Long tenantId,
                                                   @Param("keyword") String keyword,
                                                   @Param("limit") int limit,
                                                   @Param("offset") int offset);


}