package org.example.merchant_ai_operation.publicapi.store.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.merchant_ai_operation.publicapi.store.vo.PublicMarketplaceProductVO;
import org.example.merchant_ai_operation.publicapi.store.vo.PublicStoreSummaryVO;

import java.util.List;


    @Mapper
    public interface PublicStoreMapper {

        @Select("""
            SELECT
                t.id,
                t.name,
                COUNT(p.id) AS productCount
            FROM tenant t
            LEFT JOIN product_spu p
              ON p.tenant_id = t.id
             AND p.status = 'ON_SALE'
            WHERE t.status = 1
            GROUP BY
                t.id,
                t.name
            ORDER BY t.id
            """)
        List<PublicStoreSummaryVO> selectPublicStores();

        @Select("""
                SELECT
                    p.tenant_id AS storeId,
                    t.name AS storeName,
                    p.id,
                    p.name,
                    p.description,
                    MIN(s.sale_price) AS minSalePrice,
                    COALESCE(SUM(s.available_stock), 0) AS totalAvailableStock,
                    p.updated_at AS updatedAt
                FROM tenant t
                JOIN product_spu p
                  ON p.tenant_id = t.id
                 AND p.status = 'ON_SALE'
                JOIN product_sku s
                  ON s.spu_id = p.id
                 AND s.tenant_id = p.tenant_id
                 AND s.status = 'ON_SALE'
                WHERE t.status = 1
                  AND (
                      #{keyword} IS NULL
                      OR #{keyword} = ''
                      OR p.name LIKE CONCAT('%', #{keyword}, '%')
                      OR p.description LIKE CONCAT('%', #{keyword}, '%')
                      OR t.name LIKE CONCAT('%', #{keyword}, '%')
                  )
                  AND (
                      #{storeId} IS NULL
                      OR p.tenant_id = #{storeId}
                  )
                GROUP BY
                    p.tenant_id,
                    t.name,
                    p.id,
                    p.name,
                    p.description,
                    p.updated_at
                ORDER BY p.updated_at DESC
                LIMIT #{limit} OFFSET #{offset}
                """)
        List<PublicMarketplaceProductVO> searchPublicProducts(
                @Param("keyword") String keyword,
                @Param("storeId") Long storeId,
                @Param("limit") int limit,
                @Param("offset") int offset
        );



    }