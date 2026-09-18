package org.example.merchant_ai_operation.merchant.ai.tool;



import org.example.merchant_ai_operation.merchant.analytics.service.MerchantAnalyticsQueryService;
import org.example.merchant_ai_operation.merchant.analytics.vo.AfterSaleRateVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.LowStockSkuVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.OrderStatusStatisticsVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.MerchantOperatingSummaryVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.PromotionPerformanceVO;
import org.example.merchant_ai_operation.merchant.analytics.vo.TopProductVO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;


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

    @Tool(description = "查询当前登录商家在指定日期范围内销量最高的 SKU，包括商品名称、销量和已支付销售额。")
    public List<TopProductVO> getTopProducts(
            @ToolParam(description = "查询开始日期，格式为 yyyy-MM-dd")
            LocalDate startDate,
            @ToolParam(description = "查询结束日期，格式为 yyyy-MM-dd，最多查询 31 天")
            LocalDate endDate,
            @ToolParam(description = "返回热销商品数量，必须在 1 到 10 之间")
            Integer limit
    ) {
        List<TopProductVO> products = analyticsQueryService.getTopProducts(
                startDate,
                endDate,
                limit
        );

        toolUsageTracker.markBusinessDataUsed();
        return products;
    }

    @Tool(description = "查询当前登录商家在指定日期范围内的促销活动效果，包括预约数、创建订单数、成交件数、促销成交额和下单转化率。")
    public List<PromotionPerformanceVO> getPromotionPerformance(
            @ToolParam(description = "查询开始日期，格式为 yyyy-MM-dd")
            LocalDate startDate,
            @ToolParam(description = "查询结束日期，格式为 yyyy-MM-dd，最多查询 31 天")
            LocalDate endDate,
            @ToolParam(description = "返回促销活动数量，必须在 1 到 10 之间")
            Integer limit
    ) {
        List<PromotionPerformanceVO> performances =
                analyticsQueryService.getPromotionPerformance(
                        startDate,
                        endDate,
                        limit
                );

        toolUsageTracker.markBusinessDataUsed();
        return performances;
    }

    @Tool(description = "查询当前登录商家在指定日期范围内的售后概览，包括已支付订单明细数、发起售后的明细数和售后申请率。")
    public AfterSaleRateVO getAfterSaleRate(
            @ToolParam(description = "查询开始日期，格式为 yyyy-MM-dd")
            LocalDate startDate,
            @ToolParam(description = "查询结束日期，格式为 yyyy-MM-dd，最多查询 31 天")
            LocalDate endDate
    ) {
        AfterSaleRateVO afterSaleRate = analyticsQueryService.getAfterSaleRate(
                startDate,
                endDate
        );

        toolUsageTracker.markBusinessDataUsed();
        return afterSaleRate;
    }

    @Tool(description = "查询当前登录商家的低库存 SKU 当前快照，包括商品名称、规格名称、售价、可售库存和锁定库存。低库存指可售库存不高于 5。")
    public List<LowStockSkuVO> getLowStockSkus(
            @ToolParam(description = "返回低库存 SKU 数量，必须在 1 到 10 之间")
            Integer limit
    ) {
        List<LowStockSkuVO> skus = analyticsQueryService.getLowStockSkus(limit);

        toolUsageTracker.markBusinessDataUsed();
        return skus;
    }

    @Tool(description = "查询当前登录商家在指定日期范围内的订单状态统计，包括订单总数、待付款、已支付、已取消和已关闭订单数。")
    public OrderStatusStatisticsVO getOrderStatusStatistics(
            @ToolParam(description = "查询开始日期，格式为 yyyy-MM-dd")
            LocalDate startDate,
            @ToolParam(description = "查询结束日期，格式为 yyyy-MM-dd，最多查询 31 天")
            LocalDate endDate
    ) {
        OrderStatusStatisticsVO statistics =
                analyticsQueryService.getOrderStatusStatistics(startDate, endDate);

        toolUsageTracker.markBusinessDataUsed();
        return statistics;
    }
}
