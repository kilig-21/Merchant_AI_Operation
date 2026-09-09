package org.example.merchant_ai_operation.merchant.ai.service;


import lombok.extern.slf4j.Slf4j;
import org.example.merchant_ai_operation.merchant.ai.dto.AiChatRequest;
import org.example.merchant_ai_operation.merchant.ai.exception.AiChatException;
import org.example.merchant_ai_operation.merchant.ai.guard.AiChatGuard;
import org.example.merchant_ai_operation.merchant.ai.vo.AiChatResponse;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class AiChatService {

    //系统提示词
    private static final String SYSTEM_PROMPT = """
            你是商家经营助手的早期版本。
            当前不能查询订单、商品、库存、经营指标或售后数据，也不能执行任何业务操作。
            如果用户询问真实经营数据，必须明确说明当前版本尚未接入经营数据。
            使用中文回答，表达简洁，不要编造任何真实业务数字。
            """;

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final AiChatGuard aiChatGuard;
    public AiChatService(
            ObjectProvider<ChatModel> chatModelProvider,
            AiChatGuard aiChatGuard) {
        this.chatModelProvider = chatModelProvider;
        this.aiChatGuard = aiChatGuard;
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

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(request.message())
            ));

            try {
                // 调用模型。
                ChatResponse response = chatModel.call(prompt);

                if (response == null
                        || response.getResult() == null
                        || response.getResult().getOutput() == null
                        || response.getResult().getOutput().getText() == null
                        || response.getResult().getOutput().getText().isBlank()) {
                    throw new AiChatException(502, "AI 返回内容为空", null);
                }

                //提取模型回答问题
                String answer = Objects.requireNonNull(response.getResult())
                        .getOutput()
                        .getText();

                //记录回答字符数，不记录回答内容。
                long costMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.info(
                        "AI 调用成功 requestId={} model={} cost={}ms answerChars={}",
                        requestId,
                        model,
                        costMs,
                        answer.length()
                );

                return new AiChatResponse(answer, model, false);

            } catch (AiChatException ex) {
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
