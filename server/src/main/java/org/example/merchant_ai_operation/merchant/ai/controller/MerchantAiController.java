package org.example.merchant_ai_operation.merchant.ai.controller;

import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.merchant.ai.dto.AiChatRequest;
import org.example.merchant_ai_operation.merchant.ai.service.AiChatService;
import org.example.merchant_ai_operation.merchant.ai.vo.AiChatResponse;
import org.example.merchant_ai_operation.merchant.ai.vo.AiChatStreamEvent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;


/**
 * 商家 AI 对话接口。
 * 同步接口支持只读经营查询，流式接口当前输出模型文本增量。
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

    @PostMapping(
            value = "/chat/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE    //声明响应类型是 SSE
    )
    public SseEmitter chatStream(@Valid @RequestBody AiChatRequest request){
        SseEmitter emitter = new SseEmitter(30_000L);

        try {
            //发送start
            emitter.send(
                    SseEmitter.event()
                            .name("start")
                            .data(new AiChatStreamEvent(
                                    "start",
                                    Map.of("message", "SSE 连接已建立")
                            ))
            );

            //订阅模型增量流，并在模型完成后发送 done
            aiChatService.stream(request).subscribe(
                    delta -> {
                        try {
                            emitter.send(
                                    SseEmitter.event()
                                            .name("delta")
                                            .data(new AiChatStreamEvent(
                                                    "delta",
                                                    Map.of("content", delta)
                                            ))
                            );
                        } catch (IOException ex) {
                            emitter.completeWithError(ex);
                        }
                    },
                    error -> {
                        try {
                            emitter.send(
                                    SseEmitter.event()
                                            .name("error")
                                            .data(new AiChatStreamEvent(
                                                    "error",
                                                    Map.of("message", error.getMessage())
                                            ))
                            );
                        } catch (IOException ignored) {
                            // 连接已经无法继续发送
                        }
                        emitter.completeWithError(error);
                    },
                    () -> {
                        try {
                            emitter.send(
                                    SseEmitter.event()
                                            .name("done")
                                            .data(new AiChatStreamEvent(
                                                    "done",
                                                    Map.of("message", "模型回答完成")
                                            ))
                            );
                            emitter.complete();
                        } catch (IOException ex) {
                            emitter.completeWithError(ex);
                        }
                    }
            );
    }catch (IOException ex){
            emitter.completeWithError(ex);
        }
        return emitter;
    }

}