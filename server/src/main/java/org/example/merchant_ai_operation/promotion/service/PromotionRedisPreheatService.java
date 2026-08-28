package org.example.merchant_ai_operation.promotion.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.promotion.entity.PromotionActivity;
import org.example.merchant_ai_operation.promotion.entity.PromotionItem;
import org.example.merchant_ai_operation.promotion.mapper.PromotionActivityMapper;
import org.example.merchant_ai_operation.promotion.mapper.PromotionItemMapper;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Map;

@Service
public class PromotionRedisPreheatService {


    private final PromotionActivityMapper promotionActivityMapper;
    private final PromotionItemMapper promotionItemMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final Clock applicationClock;
    public PromotionRedisPreheatService(
            PromotionActivityMapper promotionActivityMapper,
            PromotionItemMapper promotionItemMapper,
            StringRedisTemplate stringRedisTemplate,
            Clock applicationClock
    ) {
        this.promotionActivityMapper = promotionActivityMapper;
        this.promotionItemMapper = promotionItemMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.applicationClock = applicationClock;
    }

    //预热查询
    public void preheat(Long activityId) {
        Long tenantId = CurrentUser.requiredMerchantTenantId();

        //查询到活动信息
        PromotionActivity activity = promotionActivityMapper.selectByIdAndTenantId(activityId,tenantId);
        if(activity == null){ throw new BizException(404, "促销活动不存在");}
        if (!"SCHEDULED".equals(activity.getStatus())) {
            throw new BizException(409, "当前活动状态不能预热");
        }
        //查询活动商品
        PromotionItem item = promotionItemMapper.selectByActivityId(activityId,tenantId);
        if(item == null){throw new BizException(500, "促销活动缺少活动商品");}

        //写入Redis
        writeRedisSnapshot(item, activity);

    }



    private void writeRedisSnapshot(PromotionItem item, PromotionActivity activity) {
        //生成两个 Redis Key
        String stockKey = stockKey(item.getId());
        String rulesKey = rulesKey(item.getId());

        //把 MySQL 中的 LocalDateTime 开始时间，按应用时区转换成“Unix 毫秒时间戳”。
        //开始时间
        long startAt = activity.getStartAt()
                .atZone(applicationClock.getZone())
                .toInstant()
                .toEpochMilli();
        //结束时间
        long endAt = activity.getEndAt()
                .atZone(applicationClock.getZone())
                .toInstant()
                .toEpochMilli();

        //写库存然后
        stringRedisTemplate.opsForValue().set(
                stockKey,
                String.valueOf(item.getStockAvailable())
        );

        //写活动规则
        stringRedisTemplate.opsForHash().putAll(
                rulesKey,
                Map.of(
                        "startAt", String.valueOf(startAt),
                        "endAt", String.valueOf(endAt),
                        "limitPerUser", String.valueOf(item.getLimitPerUser())
                )
        );
    }

    //创建key
    private String rulesKey(Long itemId) {return "promotion:item:{" + itemId + "}:rules:v1";}
    private String stockKey(Long itemId) {return "promotion:item:{" + itemId + "}:stock:v1";}
}
