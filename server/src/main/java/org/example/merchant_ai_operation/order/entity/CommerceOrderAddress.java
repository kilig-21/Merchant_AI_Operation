package org.example.merchant_ai_operation.order.entity;

import lombok.*;

import java.time.LocalDateTime;


@Data
public class CommerceOrderAddress {
    private Long id;
    private Long orderId;
    private Long consumerId;
    private Long sourceAddressId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private LocalDateTime createdAt;
}
