package org.example.merchant_ai_operation.publicapi.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicProductBaseVO;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicSkuAvailabilityVO;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicSkuVO;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicProductListItemVO;

import java.util.List;

//查询商品列表
@Mapper
public interface PublicProductMapper {
        @Select("""
            SELECT
                p.id,
                p.name,
                p.description,
                MIN(s.sale_price) AS minSalePrice,
                COALESCE(SUM(s.available_stock), 0) AS totalAvailableStock,
                p.updated_at AS updatedAt
            FROM product_spu p
            JOIN product_sku s
              ON s.spu_id = p.id
             AND s.tenant_id = p.tenant_id
             AND s.status = 'ON_SALE'
            WHERE p.tenant_id = #{storeId}
              AND p.status = 'ON_SALE'
            GROUP BY
                p.id,
                p.name,
                p.description,
                p.updated_at
            ORDER BY p.updated_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
        List<PublicProductListItemVO> selectPublicProducts(
                @Param("storeId") Long storeId,
                @Param("limit") int limit,
                @Param("offset") int offset
        );



        //第一个查 SPU：商品本体信息，比如名称、描述、更新时间。
        //这个方法只查已上架 SPU。下架商品会查不到，后面 Service 里就返回“商品不存在”。
        @Select("""
            SELECT
                p.id,
                p.name,
                p.description,
                p.updated_at AS updatedAt
            FROM product_spu p
            WHERE p.id = #{spuId}
              AND p.status = 'ON_SALE'
            """)
        //查 SPU 基础详情
        PublicProductBaseVO selectPublicProductDetail(@Param("spuId") Long spuId);




        //第二个查 SKU：这个商品下面有哪些可售规格、价格、库存。
        //查询具体的sku商品型号
        @Select("""
        SELECT
            s.id,
            s.sku_name AS skuName,
            s.sale_price AS salePrice,
            s.available_stock AS availableStock
        FROM product_sku s
        JOIN product_spu p
          ON p.id = s.spu_id
         AND p.tenant_id = s.tenant_id
        WHERE s.spu_id = #{spuId}
          AND s.status = 'ON_SALE'
          AND p.status = 'ON_SALE'
        ORDER BY s.sale_price ASC 
        """)                    //sql里默认的是升序,所以ASC有点冗余
        //查这个 SPU 下的可售 SKU
        List<PublicSkuVO> selectPublicSkusBySpuId(@Param("spuId") Long spuId);

        @Select("""
        SELECT
            s.id AS skuId,
            CASE
                WHEN s.available_stock > 0 THEN TRUE
                ELSE FALSE
            END AS purchasable,
            s.available_stock AS availableStock,
            CASE
                WHEN s.available_stock > 0 THEN '可购买'
                ELSE '库存不足'
            END AS message
        FROM product_sku s
        JOIN product_spu p
          ON p.id = s.spu_id
         AND p.tenant_id = s.tenant_id
        WHERE s.id = #{skuId}
          AND s.status = 'ON_SALE'
          AND p.status = 'ON_SALE'
        """)
        PublicSkuAvailabilityVO selectSkuAvailability(@Param("skuId") Long skuId);

}


