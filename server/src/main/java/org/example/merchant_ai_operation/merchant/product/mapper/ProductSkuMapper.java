package org.example.merchant_ai_operation.merchant.product.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;

@Mapper
public interface ProductSkuMapper {

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
}