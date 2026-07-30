package org.example.merchant_ai_operation.merchant.product.mapper;

import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.order.vo.OrderSkuSnapshotVO;

import java.util.List;

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

    @Select("""
            <script>
            SELECT
                ci.id AS cartItemId,
                ps.id AS skuId,
                ps.tenant_id AS tenantId,
                ps.sku_name AS skuName,
                ps.sale_price AS salePrice,
                ci.quantity AS quantity,
                ps.available_stock AS availableStock,
                ps.status AS skuStatus,
                spu.status AS spuStatus
            FROM cart_item ci
            JOIN product_sku ps ON ci.sku_id = ps.id
            JOIN product_spu spu ON ps.spu_id = spu.id
            WHERE ci.consumer_id = #{consumerId}
              AND ci.id IN
              <foreach collection="cartItemIds" item="cartItemId" open="(" separator="," close=")">
                  #{cartItemId}
              </foreach>
            </script>
            """)
    //下单前拍一张当前商品和购物车状态的照片
    List<OrderSkuSnapshotVO> selectOrderSkuSnapshots(
            @Param("consumerId") Long consumerId,
            @Param("cartItemIds") List<Long> cartItemIds
    );


    //AND available_stock >= #{quantity}
    //这句让扣库存变成原子操作。多个用户同时抢同一个 SKU 时，数据库会保证只有库存足够的更新能成功。成功返回 1，失败返回 0。
    @Update("""
            UPDATE product_sku
            SET available_stock = available_stock - #{quantity},
                locked_stock = locked_stock + #{quantity},
                version = version + 1
            WHERE id = #{skuId}
              AND tenant_id = #{tenantId}
              AND status = 'ON_SALE'
              AND available_stock >= #{quantity}
            """)
    //条件扣库存
    int lockStock(
            @Param("skuId") Long skuId,
            @Param("tenantId") Long tenantId,
            @Param("quantity") Integer quantity
    );

}