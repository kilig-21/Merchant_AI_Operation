package org.example.merchant_ai_operation.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest (
        @NotBlank(message = "收货人姓名不能为空")
        @Size(max = 64, message = "收货人姓名不能超过64个字符")
        String receiverName,

        @NotBlank(message = "收货人手机号不能为空")
        @Size(max = 32, message = "手机号不能超过32个字符")
        String receiverPhone,

        @NotBlank(message = "省份不能为空")
        @Size(max = 64, message = "省份不能超过64个字符")
        String province,

        @NotBlank(message = "城市不能为空")
        @Size(max = 64, message = "城市不能超过64个字符")
        String city,

        @NotBlank(message = "区县不能为空")
        @Size(max = 64, message = "区县不能超过64个字符")
        String district,

        @NotBlank(message = "详细地址不能为空")
        @Size(max = 255, message = "详细地址不能超过255个字符")
        String detailAddress,

        Boolean isDefault
) {}
