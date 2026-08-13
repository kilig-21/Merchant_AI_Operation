package org.example.merchant_ai_operation.promotion.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.security.CurrentUser;
import java.time.Instant;
import java.util.UUID;
import org.example.merchant_ai_operation.promotion.dto.PromotionReservationResult;
import org.example.merchant_ai_operation.promotion.dto.ReservePromotionRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class PromotionReservationService {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> promotionReserveScript;
    private final Clock applicationClock;

    public PromotionReservationService(
            StringRedisTemplate stringRedisTemplate,
            DefaultRedisScript<List> promotionReserveScript,
            Clock applicationClock
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.promotionReserveScript = promotionReserveScript;
        this.applicationClock = applicationClock;
    }

    public PromotionReservationResult reserve(ReservePromotionRequest request) {

        // 从安全上下文和抢购请求取得执行 Lua 所需的用户、活动商品、资格编号与服务端时间参数
        Long consumerId = CurrentUser.requiredConsumerId();
        Long itemId = request.activityItemId();
        String reservationId = UUID.randomUUID().toString();
        long nowMillis = Instant.now(applicationClock).toEpochMilli();

        List<?> result = insertLua(request, itemId, consumerId, nowMillis, reservationId);

        //result == null 要满足上线后lua是空白的
        if (result == null || result.size() != 2) {
            throw new IllegalStateException("抢购 Lua 脚本返回结果异常");
        }

        int code = Integer.parseInt(String.valueOf(result.get(0)));
        String resultReservationId = String.valueOf(result.get(1));

        // 返回结果不是约定的 {返回码, reservationId} 两项结构时，停止处理。
        switch (code) {
            case 1, 2 -> {
                return new PromotionReservationResult(code, resultReservationId);
            }
            case -1 -> throw new BizException(409, "活动尚未预热");
            case -2 -> throw new BizException(409, "活动尚未开始");
            case -3 -> throw new BizException(409, "活动已结束");
            case -4 -> throw new BizException(409, "活动库存不足或已售罄");
            case -5 -> throw new BizException(409, "超过单用户限购");
            default -> throw new IllegalStateException("未知的抢购 Lua 返回码：" + code);
        }

    }


    //<-------------提取方法-------------->


    private  List<?> insertLua(
            ReservePromotionRequest request,
            Long itemId,
            Long consumerId,
            long nowMillis,
            String reservationId
    ) {
        List<String> keys = List.of(
                rulesKey(itemId),
                stockKey(itemId),
                userQuantityKey(itemId, consumerId),
                requestKey(itemId, request.requestKey())
        );

        return stringRedisTemplate.execute(
                promotionReserveScript,
                keys,
                String.valueOf(nowMillis),
                String.valueOf(request.quantity()),
                reservationId
        );
    }

    //规则key
    private String rulesKey(Long itemId) {
        return "promotion:item:{" + itemId + "}:rules:v1";
    }

    //库存key
    private String stockKey(Long itemId) {
        return "promotion:item:{" + itemId + "}:stock:v1";
    }

    //用户数量key
    private String userQuantityKey(Long itemId, Long consumerId) {
        return "promotion:item:{" + itemId + "}:user:" + consumerId + ":v1";
    }

    //请求key
    private String requestKey(Long itemId, String requestKey) {
        return "promotion:item:{" + itemId + "}:request:" + requestKey + ":v1";
    }


}
