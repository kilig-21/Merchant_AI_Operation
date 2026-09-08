package org.example.merchant_ai_operation.merchant.ai.vo;

public record AiChatResponse(
        String answer,
        String model,
        boolean businessDataUsed
)
{}
