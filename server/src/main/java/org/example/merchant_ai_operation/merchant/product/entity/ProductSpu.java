package org.example.merchant_ai_operation.merchant.product.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
//对用数据库里的 product_spu 表
public class ProductSpu {
    private Long id;
    private Long tenantId;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
