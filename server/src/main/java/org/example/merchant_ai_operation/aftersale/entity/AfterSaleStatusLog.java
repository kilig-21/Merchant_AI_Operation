package org.example.merchant_ai_operation.aftersale.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AfterSaleStatusLog {
    private Long id;
    private Long afterSaleId;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String operatorType;
    private String remark;
    private LocalDateTime createdAt;
}