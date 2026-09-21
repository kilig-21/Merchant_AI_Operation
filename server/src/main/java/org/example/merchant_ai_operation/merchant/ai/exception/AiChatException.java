package org.example.merchant_ai_operation.merchant.ai.exception;


import lombok.Getter;

/**
 * AI 调用过程中的业务异常。
 */
@Getter
public class AiChatException extends RuntimeException {

    private final int code;

    public AiChatException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
