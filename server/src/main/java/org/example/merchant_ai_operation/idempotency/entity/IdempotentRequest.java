package org.example.merchant_ai_operation.idempotency.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IdempotentRequest {
    private Long id;
    private Long consumerId;
    private String requestKey;
    private String requestHash;
    private String status;
    private Long orderId;
    private String responseBody;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
