package org.example.merchant_ai_operation.promotion.task;


import lombok.extern.slf4j.Slf4j;
import org.example.merchant_ai_operation.promotion.compensation.entity.PromotionCompensationRecord;
import org.example.merchant_ai_operation.promotion.compensation.mapper.PromotionCompensationRecordMapper;
import org.example.merchant_ai_operation.promotion.compensation.service.PromotionCompensationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PromotionCompensationRetryJob {
    private static final int BATCH_SIZE = 100;

    private final PromotionCompensationRecordMapper compensationRecordMapper;
    private final PromotionCompensationService compensationService;
    public PromotionCompensationRetryJob(
            PromotionCompensationRecordMapper compensationRecordMapper,
            PromotionCompensationService compensationService
    ) {
        this.compensationRecordMapper = compensationRecordMapper;
        this.compensationService = compensationService;
    }


    /*
     PENDING 补偿记录
     → 每 10 秒扫描
     → 重新执行 Lua
     → 成功后改为 COMPLETED
     */
    @Scheduled(fixedDelay = 10_000)
    public void retryPendingCompensations(){
        List<PromotionCompensationRecord> records =
                compensationRecordMapper.selectPendingRecords(BATCH_SIZE);

        for (PromotionCompensationRecord record : records) {
            try {
                compensationService.executePendingCompensation(record);
            } catch (Exception e) {
                log.error(
                        "促销补偿重试失败，reservationId={}",
                        record.getReservationId(),
                        e
                );
            }
        }
    }
}
