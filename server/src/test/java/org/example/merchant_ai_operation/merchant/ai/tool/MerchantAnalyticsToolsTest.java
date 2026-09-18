package org.example.merchant_ai_operation.merchant.ai.tool;

import org.example.merchant_ai_operation.merchant.analytics.service.MerchantAnalyticsQueryService;
import org.example.merchant_ai_operation.merchant.analytics.vo.MerchantOperatingSummaryVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantAnalyticsToolsTest {

    @Test
    void delegatesOperatingSummaryQueryToAnalyticsService() {
        MerchantAnalyticsQueryService analyticsQueryService =
                mock(MerchantAnalyticsQueryService.class);

        AiToolUsageTracker toolUsageTracker = new AiToolUsageTracker();

        MerchantAnalyticsTools tools =
                new MerchantAnalyticsTools(
                        analyticsQueryService,
                        toolUsageTracker
                );

        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);

        MerchantOperatingSummaryVO expected =
                new MerchantOperatingSummaryVO(
                        3L,
                        2L,
                        new BigDecimal("350.00"),
                        new BigDecimal("175.00"),
                        1L,
                        1L
                );

        when(analyticsQueryService.getOperatingSummary(startDate, endDate))
                .thenReturn(expected);

        try (AiToolUsageTracker.Scope scope = toolUsageTracker.openScope()) {
            MerchantOperatingSummaryVO actual =
                    tools.getOperatingSummary(startDate, endDate);

            assertSame(expected, actual);
            assertTrue(scope.businessDataUsed());
        }

        verify(analyticsQueryService)
                .getOperatingSummary(startDate, endDate);
    }
}
