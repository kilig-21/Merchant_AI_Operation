package org.example.merchant_ai_operation.publicapi.promotion.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.publicapi.promotion.mapper.PublicPromotionMapper;
import org.example.merchant_ai_operation.publicapi.promotion.vo.PublicPromotionActivityDetailVO;
import org.example.merchant_ai_operation.publicapi.promotion.vo.PublicPromotionActivityListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicPromotionService {

    private final PublicPromotionMapper publicPromotionMapper;
    private final Clock applicationClock;

    public PublicPromotionService(
            PublicPromotionMapper publicPromotionMapper,
            Clock applicationClock
    ) {
        this.publicPromotionMapper = publicPromotionMapper;
        this.applicationClock = applicationClock;
    }

    // 查询所有消费者可见活动，并附带服务器时间供前端倒计时使用。
    @Transactional(readOnly = true)
    public PublicPromotionActivityListVO listVisibleActivities() {
        return new PublicPromotionActivityListVO(
                LocalDateTime.now(applicationClock),
                publicPromotionMapper.selectVisibleActivities()
        );
    }

    // 查询指定消费者可见活动；不可见活动统一按不存在处理。
    @Transactional(readOnly = true)
    public PublicPromotionActivityDetailVO getVisibleActivity(Long activityId) {
        var activity = publicPromotionMapper.selectVisibleActivityById(activityId);

        if (activity == null) {throw new BizException(404, "促销活动不存在");}

        return new PublicPromotionActivityDetailVO(
                LocalDateTime.now(applicationClock),
                activity
        );
    }
}