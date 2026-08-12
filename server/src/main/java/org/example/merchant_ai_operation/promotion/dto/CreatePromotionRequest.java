package org.example.merchant_ai_operation.promotion.dto;


import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
注意两点：
tenantId 不放在请求 DTO 里，必须从当前登录商家的 JWT 中取得。
@Future 只能判断时间晚于当前时间，不能判断 startAt < endAt，所以开始时间和结束时间的先后关系仍要在 Service 中判断。
*/
public record CreatePromotionRequest(
        @NotBlank(message = "活动名称不能为空")
        @Size(max = 128, message = "活动名称不能超过128个字符")
        String name,

        @NotNull(message = "活动开始时间不能为空")
        @Future(message = "活动开始时间必须晚于当前时间")
        LocalDateTime startAt,

        @NotNull(message = "活动结束时间不能为空")
        @Future(message = "活动结束时间必须晚于当前时间")
        LocalDateTime endAt,

        @NotNull(message = "SKU不能为空")
        Long skuId,

        @NotNull(message = "活动价格不能为空")
        @DecimalMin(value = "0.01", message = "活动价格必须大于0")
        BigDecimal activityPrice,

        @NotNull(message = "活动库存不能为空")
        @Min(value = 1, message = "活动库存必须大于0")
        Integer stockTotal,

        @NotNull(message = "每人限购数量不能为空")
        @Min(value = 1, message = "每人限购数量必须大于0")
        Integer limitPerUser

) {}
