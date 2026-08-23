package org.example.merchant_ai_operation.order.vo;


//返回订单历史所需的收货信息，不返回地址主键和消费者内部 ID
public record OrderAddressSnapshotVO (
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddress
) {}
