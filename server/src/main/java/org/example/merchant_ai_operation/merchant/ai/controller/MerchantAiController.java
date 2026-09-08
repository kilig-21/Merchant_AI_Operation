package org.example.merchant_ai_operation.merchant.ai.controller;

import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.merchant.ai.dto.AiChatRequest;
import org.example.merchant_ai_operation.merchant.ai.service.AiChatService;
import org.example.merchant_ai_operation.merchant.ai.vo.AiChatResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 商家 AI 对话接口。
 * 当前版本只调用模型，不读取经营数据，也不执行业务操作。
 */
@RestController
@RequestMapping("/api/merchant/ai")
public class MerchantAiController {

    private final AiChatService aiChatService;
    public MerchantAiController(AiChatService aiChatService) {this.aiChatService = aiChatService;}

    /**
     * 接收当前商家的文本问题接口，并返回模型回答。
     */
    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request
    ) {
        return ApiResponse.ok(aiChatService.chat(request));
    }
}