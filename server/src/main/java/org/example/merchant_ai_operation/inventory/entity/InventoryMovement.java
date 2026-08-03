package org.example.merchant_ai_operation.inventory.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryMovement {
    private Long id;
    private Long tenantId;
    private Long skuId;
    private String businessType;
    private String businessNo;
    private Integer availableChange;
    private Integer lockedChange;
    private Integer availableAfter;
    private Integer lockedAfter;
    private LocalDateTime createdAt;
}