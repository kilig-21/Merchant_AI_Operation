package org.example.merchant_ai_operation.merchant.ai.vo;

/**
*   事件协议 DTO
* */
public record AiChatStreamEvent(
        String type,
        Object data
) {
}