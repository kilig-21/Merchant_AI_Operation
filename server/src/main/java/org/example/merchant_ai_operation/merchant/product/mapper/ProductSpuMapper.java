package org.example.merchant_ai_operation.merchant.product.mapper;


import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSpu;
import org.example.merchant_ai_operation.merchant.product.vo.MerchantProductVO;

import java.util.List;

@Mapper
public interface ProductSpuMapper {

    //新增商品类型
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
                p.id,
                p.name,
                p.description,
                p.status,
                p.created_at AS createdAt,
                p.updated_at AS updatedAt,
                COUNT(s.id) AS skuCount,
                MIN(s.sale_price) AS minSalePrice,
                COALESCE(SUM(s.available_stock), 0) AS totalAvailableStock
            FROM product_spu p
            LEFT JOIN product_sku s
              ON s.spu_id = p.id
             AND s.tenant_id = p.tenant_id
            WHERE p.tenant_id = #{tenantId}
              AND (
                  #{keyword} IS NULL
                  OR #{keyword} = ''
                  OR p.name LIKE CONCAT('%', #{keyword}, '%')
              )
            GROUP BY
                p.id,
                p.name,
                p.description,
                p.status,
                p.created_at,
                p.updated_at
            ORDER BY p.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<MerchantProductVO> selectMerchantProducts(@Param("tenantId") Long tenantId,
                                                   @Param("keyword") String keyword,
                                                   @Param("limit") int limit,
                                                   @Param("offset") int offset);


    //不是只按商品 ID 改，而是按 商品 ID + 当前商家 tenantId 改。
    // 这样商家 A 拿到商家 B 的商品 ID，也改不到 B 的商品。
    @Update("""
        UPDATE product_spu
        SET status = #{status}
        WHERE id = #{spuId}
          AND tenant_id = #{tenantId}
        """)
    int updateStatusByIdAndTenantId(@Param("spuId") Long spuId,
                                 @Param("tenantId") Long tenantId,
                                 @Param("status") String status);




}