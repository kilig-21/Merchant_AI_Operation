package org.example.merchant_ai_operation.merchant.product.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;

@Mapper
public interface ProductSkuMapper {

    //新增具体商品
    @Insert("""
            INSERT INTO product_sku (
                id,
                tenant_id,
                spu_id,
                sku_name,
                sale_price,
                available_stock,
                locked_stock,
                version,
                status
            )
            VALUES (
                #{sku.id},
                #{sku.tenantId},
                #{sku.spuId},
                #{sku.skuName},
                #{sku.salePrice},
                #{sku.availableStock},
                #{sku.lockedStock},
                #{sku.version},
                #{sku.status}
            )
            """)
    int insert(@Param("sku") ProductSku sku);


    //COUNT(1)会返回符合条件的行数
    //如果结果是 0，说明没有 SKU，不能上架。
    //如果结果大于 0，说明至少有一个 SKU，可以继续上架。
    @Select("""
        SELECT COUNT(1)
        FROM product_sku
        WHERE spu_id = #{spuId}
          AND tenant_id = #{tenantId}
        """)
    int countBySpuIdAndTenantId(@Param("spuId") Long spuId,
                                @Param("tenantId") Long tenantId);

}