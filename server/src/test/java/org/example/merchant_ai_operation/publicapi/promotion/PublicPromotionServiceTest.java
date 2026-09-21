package org.example.merchant_ai_operation.publicapi.promotion;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.publicapi.promotion.mapper.PublicPromotionMapper;
import org.example.merchant_ai_operation.publicapi.promotion.service.PublicPromotionService;
import org.example.merchant_ai_operation.publicapi.promotion.vo.PublicPromotionActivityItemVO;
import org.example.merchant_ai_operation.publicapi.promotion.vo.PublicPromotionActivityListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicPromotionServiceTest {

    private static final LocalDateTime SERVER_TIME = LocalDateTime.of(2026, 9, 4, 17, 0);

    @Mock
    private PublicPromotionMapper publicPromotionMapper;

    private PublicPromotionService publicPromotionService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-04T09:00:00Z"),
                ZoneId.of("Asia/Shanghai")
        );
        publicPromotionService = new PublicPromotionService(publicPromotionMapper, clock);
    }

    @Test
    void listVisibleActivitiesReturnsServerTimeAndMapperResults() {
        PublicPromotionActivityItemVO activity = activity();
        when(publicPromotionMapper.selectVisibleActivities()).thenReturn(List.of(activity));

        PublicPromotionActivityListVO result = publicPromotionService.listVisibleActivities();

        assertEquals(SERVER_TIME, result.serverTime());
        assertEquals(List.of(activity), result.activities());
        verify(publicPromotionMapper).selectVisibleActivities();
    }

    @Test
    void getVisibleActivityReturnsActivity() {
        PublicPromotionActivityItemVO activity = activity();
        when(publicPromotionMapper.selectVisibleActivityById(25L)).thenReturn(activity);

        var result = publicPromotionService.getVisibleActivity(25L);

        assertEquals(SERVER_TIME, result.serverTime());
        assertEquals(activity, result.activity());
        verify(publicPromotionMapper).selectVisibleActivityById(25L);
    }

    @Test
    void getVisibleActivityMapsMissingActivityToNotFound() {
        when(publicPromotionMapper.selectVisibleActivityById(404L)).thenReturn(null);

        BizException exception = assertThrows(
                BizException.class,
                () -> publicPromotionService.getVisibleActivity(404L)
        );

        assertEquals(404, exception.getCode());
        assertEquals("促销活动不存在", exception.getMessage());
    }

    private static PublicPromotionActivityItemVO activity() {
        return new PublicPromotionActivityItemVO(
                25L,
                31L,
                "秋日限量活动",
                "降噪耳机",
                "黑色",
                new BigDecimal("99.00"),
                SERVER_TIME.plusHours(1),
                SERVER_TIME.plusHours(2),
                "SCHEDULED",
                "AVAILABLE",
                1
        );
    }
}
