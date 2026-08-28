package org.example.merchant_ai_operation.promotion.task;

import lombok.extern.slf4j.Slf4j;
import org.example.merchant_ai_operation.promotion.mapper.PromotionActivityMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;


@Component
@Slf4j
public class PromotionStatusRecoveryJob {
    private final PromotionActivityMapper promotionActivityMapper;
    private final Clock applicationClock;

    public PromotionStatusRecoveryJob(
            PromotionActivityMapper promotionActivityMapper,
            Clock applicationClock
    ) {
        this.promotionActivityMapper = promotionActivityMapper;
        this.applicationClock = applicationClock;
    }

    @Scheduled(fixedRate = 30_000)
    public void refreshPromotionStatuses() {
        LocalDateTime now = LocalDateTime.now(applicationClock);


        //取得活动开始的信号
        int activated = promotionActivityMapper.markScheduledAsActive(now);

        //取得活动结束的信号
        int ended = promotionActivityMapper.markExpiredAsEnded(now);

        if (activated > 0 ||ended > 0) {
            log.info(
                    "促销状态推进完成，activated={}, ended={}, now={}",
                    activated,
                    ended,
                    now
            );
        }
    }
}
