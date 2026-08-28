package org.example.merchant_ai_operation.aftersale.entity;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AfterSaleRequest {
    private Long id;
    private String requestNo;
    private Long orderId;
    private Long orderItemId;
    private Long tenantId;
    private Long consumerId;
    private Integer quantity;
    private BigDecimal requestedAmount;
    private String reason;
    private String status;
    private String merchantRemark;
    private Long decidedBy;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
