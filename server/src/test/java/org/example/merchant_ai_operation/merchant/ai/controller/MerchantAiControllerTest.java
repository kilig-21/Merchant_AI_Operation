package org.example.merchant_ai_operation.merchant.ai.controller;

import org.example.merchant_ai_operation.merchant.ai.dto.AiChatRequest;
import org.example.merchant_ai_operation.merchant.ai.exception.AiChatException;
import org.example.merchant_ai_operation.merchant.ai.service.AiChatService;
import org.example.merchant_ai_operation.merchant.ai.vo.AiChatResponse;
import org.example.merchant_ai_operation.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantAiController.class)
@AutoConfigureMockMvc(addFilters = false)
class MerchantAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiChatService aiChatService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void returnsChatResponse() throws Exception {
        when(aiChatService.chat(any(AiChatRequest.class)))
                .thenReturn(new AiChatResponse(
                        "当前版本可以提供通用经营建议。",
                        "deepseek-v4-flash",
                        false
                ));

        mockMvc.perform(post("/api/merchant/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"如何改善经营？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.answer").value("当前版本可以提供通用经营建议。"))
                .andExpect(jsonPath("$.data.model").value("deepseek-v4-flash"))
                .andExpect(jsonPath("$.data.businessDataUsed").value(false));
    }

    @Test
    void rejectsBlankMessageBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/merchant/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"   "}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("问题不能为空"));
    }

    @Test
    void rejectsMessageLongerThanOneThousandCharacters() throws Exception {
        String longMessage = "问".repeat(1001);

        mockMvc.perform(post("/api/merchant/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"" + longMessage + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("问题不能超过1000个字符"));
    }

    @Test
    void returnsStableAiErrorResponse() throws Exception {
        when(aiChatService.chat(any(AiChatRequest.class)))
                .thenThrow(new AiChatException(
                        503,
                        "AI 服务尚未配置，请联系管理员",
                        null
                ));

        mockMvc.perform(post("/api/merchant/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"你好"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(503))
                .andExpect(jsonPath("$.message").value("AI 服务尚未配置，请联系管理员"));
    }
}
