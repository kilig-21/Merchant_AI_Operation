package org.example.merchant_ai_operation.merchant.ai.tool;



import org.example.merchant_ai_operation.merchant.analytics.service.MerchantAnalyticsQueryService;
import org.example.merchant_ai_operation.merchant.analytics.vo.MerchantOperatingSummaryVO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.time.LocalDate;


@Component
public class MerchantAnalyticsTools {
    private final MerchantAnalyticsQueryService analyticsQueryService;
    private final AiToolUsageTracker toolUsageTracker;

    public MerchantAnalyticsTools(
            MerchantAnalyticsQueryService analyticsQueryService,
            AiToolUsageTracker toolUsageTracker
    ) {
        this.analyticsQueryService = analyticsQueryService;
        this.toolUsageTracker = toolUsageTracker;
    }

    @Tool(description = "查询当前登录商家在指定日期范围内的经营汇总，包括有效订单数、已支付订单数、营业额、客单价、待付款订单数和低库存商品数。")
    public MerchantOperatingSummaryVO getOperatingSummary(
            @ToolParam(description = "查询开始日期，格式为 yyyy-MM-dd")
            LocalDate startDate,
            @ToolParam(description = "查询结束日期，格式为 yyyy-MM-dd，最多查询 31 天")
            LocalDate endDate
    ){
        //调用已有的经营查询服务，按日期查询当前商家的真实经营汇总，并保存到 summary
        MerchantOperatingSummaryVO summary = analyticsQueryService.getOperatingSummary(startDate, endDate);

        //记录本次 AI 对话确实调用了经营数据工具
        toolUsageTracker.markBusinessDataUsed();

        //把查询结果返回给 Spring AI
        return summary;
    }
}
