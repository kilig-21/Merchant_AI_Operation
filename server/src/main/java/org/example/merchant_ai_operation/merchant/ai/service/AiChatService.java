package org.example.merchant_ai_operation.merchant.ai.service;


import lombok.extern.slf4j.Slf4j;
import org.example.merchant_ai_operation.merchant.ai.dto.AiChatRequest;
import org.example.merchant_ai_operation.merchant.ai.exception.AiChatException;
import org.example.merchant_ai_operation.merchant.ai.guard.AiChatGuard;
import org.example.merchant_ai_operation.merchant.ai.tool.AiToolUsageTracker;
import org.example.merchant_ai_operation.merchant.ai.tool.MerchantAnalyticsTools;
import org.example.merchant_ai_operation.merchant.ai.vo.AiChatResponse;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@Service
public class AiChatService {

    //系统提示词
    private static final String SYSTEM_PROMPT =
            """
            你是商家经营助手。
            仅当用户询问当前登录商家在明确日期范围内的经营汇总、热销商品、促销活动效果或售后概览时，才可以调用已提供的经营查询工具。
            工具只返回当前商家的经营汇总、热销 SKU 的名称销量销售额、促销活动的预约下单成交转化数据，或售后申请率。
            真实经营数字只能来自工具返回结果；没有调用工具时不得编造数字。
            每次对话最多调用一次经营查询工具。
            不得查询其他商家数据，不得调用任意 SQL，不得执行改价、上架、促销、订单、库存或售后写操作。
            用户的问题超出已提供工具能力时，要明确说明当前不能查询。
            使用中文回答，表达简洁。
            """;

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final AiChatGuard aiChatGuard;
    private final MerchantAnalyticsTools merchantAnalyticsTools;
    private final AiToolUsageTracker toolUsageTracker;

    public AiChatService(
            ObjectProvider<ChatModel> chatModelProvider,
            AiChatGuard aiChatGuard,
            MerchantAnalyticsTools merchantAnalyticsTools,
            AiToolUsageTracker toolUsageTracker
    ) {
        this.chatModelProvider = chatModelProvider;
        this.aiChatGuard = aiChatGuard;
        this.merchantAnalyticsTools = merchantAnalyticsTools;
        this.toolUsageTracker = toolUsageTracker;
    }

    /**
     * 调用文本模型回答商家问题。
     */
    public AiChatResponse chat(AiChatRequest request) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();

        if (chatModel == null) {
            throw new AiChatException(
                    503,
                    "AI 服务尚未配置，请联系管理员",
                    null
            );
        }

        //获取上下文信息
        Long userId = CurrentUser.required().userId();

        //记录使用的是那个模型
        try (AiChatGuard.Permit ignored = aiChatGuard.acquire(userId)) {
            String requestId = UUID.randomUUID().toString();

            String model = chatModel.getOptions().getModel();
            long startNanos = System.nanoTime();
            log.info(
                    "AI 调用开始 requestId={} model={}",
                    requestId,
                    model
            );


            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultSystem(SYSTEM_PROMPT)
                    .defaultTools(merchantAnalyticsTools)
                    .build();

            try (AiToolUsageTracker.Scope toolScope =
                         toolUsageTracker.openScope()) {

                String answer = chatClient.prompt()
                        .user(request.message())
                        .call()
                        .content();

                if (answer == null || answer.isBlank()) {
                    throw new AiChatException(502, "AI 返回内容为空", null);
                }

                boolean businessDataUsed = toolScope.businessDataUsed();

                //写入成功日志
                long costMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info(
                        "AI 调用成功 requestId={} model={} cost={}ms answerChars={} businessDataUsed={}",
                        requestId,
                        model,
                        costMs,
                        answer.length(),
                        businessDataUsed
                );

                // 保留原有成功日志，并补上 businessDataUsed
                return new AiChatResponse(answer, model, businessDataUsed);
            }
            catch (AiChatException ex) {
                //处理当前的“模型返回空内容”等已知业务问题。
                long costMs = (System.nanoTime() - startNanos) / 1_000_000;

                log.warn(
                        "AI 调用失败 requestId={} model={} cost={}ms code={} reason={}",
                        requestId,
                        model,
                        costMs,
                        ex.getCode(),
                        ex.getMessage()
                );

                throw ex;
            } catch (Exception ex) {
                //处理当前的未知失败记录异常
                long costMs = (System.nanoTime() - startNanos) / 1_000_000;

                log.warn(
                        "AI 调用异常 requestId={} model={} cost={}ms exception={}",
                        requestId,
                        model,
                        costMs,
                        ex.getClass().getSimpleName()
                );

                throw new AiChatException(
                        503,
                        "AI 服务暂时不可用，请稍后重试",
                        ex
                );
            }
        }
    }
}
