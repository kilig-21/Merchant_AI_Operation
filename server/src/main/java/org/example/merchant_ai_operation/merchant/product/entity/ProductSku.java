package org.example.merchant_ai_operation.merchant.product.entity;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductSku {
    private Long id;
    private Long tenantId;
    private Long spuId;
    private String skuName;
    private BigDecimal salePrice;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer version;
    private String status;

}
