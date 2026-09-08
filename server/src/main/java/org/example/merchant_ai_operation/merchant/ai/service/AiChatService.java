package org.example.merchant_ai_operation.merchant.ai.service;


import org.example.merchant_ai_operation.merchant.ai.dto.AiChatRequest;
import org.example.merchant_ai_operation.merchant.ai.vo.AiChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class AiChatService {

    //系统提示词
    private static final String SYSTEM_PROMPT = """
            你是商家经营助手的早期版本。
            当前不能查询订单、商品、库存、经营指标或售后数据，也不能执行任何业务操作。
            如果用户询问真实经营数据，必须明确说明当前版本尚未接入经营数据。
            使用中文回答，表达简洁，不要编造任何真实业务数字。
            """;

    private final ChatModel chatModel;
    public AiChatService(ChatModel chatModel) {this.chatModel = chatModel;}

    /**
     * 调用文本模型回答商家问题。
     */
    public AiChatResponse chat(AiChatRequest request){
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(request.message())
        ));

        // 调用模型。
        ChatResponse response = chatModel.call(prompt);

        //提取模型回答问题
        String answer = Objects.requireNonNull(response.getResult())
                .getOutput()
                .getText();

        // 获取本次使用的模型名称
        String model = chatModel.getOptions().getModel();

        return new AiChatResponse(answer, model, false);
    }
}
