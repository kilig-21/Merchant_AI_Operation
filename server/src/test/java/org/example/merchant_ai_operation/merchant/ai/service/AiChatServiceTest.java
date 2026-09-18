package org.example.merchant_ai_operation.merchant.ai.service;

import org.example.merchant_ai_operation.merchant.ai.dto.AiChatRequest;
import org.example.merchant_ai_operation.merchant.ai.exception.AiChatException;
import org.example.merchant_ai_operation.merchant.ai.guard.AiChatGuard;
import org.example.merchant_ai_operation.merchant.ai.tool.AiToolUsageTracker;
import org.example.merchant_ai_operation.merchant.ai.tool.MerchantAnalyticsTools;
import org.example.merchant_ai_operation.merchant.ai.vo.AiChatResponse;
import org.example.merchant_ai_operation.merchant.analytics.service.MerchantAnalyticsQueryService;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiChatServiceTest {

    private ObjectProvider<ChatModel> chatModelProvider;
    private ChatModel chatModel;
    private MerchantAnalyticsTools merchantAnalyticsTools;
    private AiToolUsageTracker toolUsageTracker;
    private AiChatService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        chatModelProvider = mock(ObjectProvider.class);
        chatModel = mock(ChatModel.class);
        ChatOptions options = ChatOptions.builder()
                .model("deepseek-v4-flash")
                .build();
        when(chatModel.getOptions()).thenReturn(options);
        when(chatModel.getDefaultOptions()).thenReturn(options);

        AiChatGuard guard = new AiChatGuard(Clock.systemUTC());
        toolUsageTracker = new AiToolUsageTracker();
        merchantAnalyticsTools = new MerchantAnalyticsTools(
                mock(MerchantAnalyticsQueryService.class),
                toolUsageTracker
        );
        service = new AiChatService(
                chatModelProvider,
                guard,
                merchantAnalyticsTools,
                toolUsageTracker
        );

        LoginPrincipal principal = new LoginPrincipal(8001L, 1001L, "MERCHANT_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MERCHANT_ADMIN"))
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reportsUnavailableWhenChatModelIsDisabled() {
        when(chatModelProvider.getIfAvailable()).thenReturn(null);

        AiChatException exception = assertThrows(
                AiChatException.class,
                () -> service.chat(new AiChatRequest("你好"))
        );

        assertEquals(503, exception.getCode());
        assertEquals("AI 服务尚未配置，请联系管理员", exception.getMessage());
    }

    @Test
    void returnsModelAnswerWithoutClaimingBusinessDataUsageWhenNoToolIsCalled() {
        when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
        when(chatModel.call(org.mockito.ArgumentMatchers.any(Prompt.class)))
                .thenReturn(responseWithText("可以先从复盘商品表现开始。"));

        AiChatResponse response = service.chat(new AiChatRequest("如何复盘经营？"));

        assertEquals("可以先从复盘商品表现开始。", response.answer());
        assertEquals("deepseek-v4-flash", response.model());
        assertFalse(response.businessDataUsed());

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        Prompt prompt = captor.getValue();
        assertTrue(prompt.getSystemMessage().getText().contains("仅当用户询问当前登录商家"));
        assertEquals("如何复盘经营？", prompt.getUserMessage().getText());
    }

    @Test
    void mapsBlankModelAnswerToBadGateway() {
        when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
        when(chatModel.call(org.mockito.ArgumentMatchers.any(Prompt.class)))
                .thenReturn(responseWithText("   "));

        AiChatException exception = assertThrows(
                AiChatException.class,
                () -> service.chat(new AiChatRequest("你好"))
        );

        assertEquals(502, exception.getCode());
        assertEquals("AI 返回内容为空", exception.getMessage());
    }

    @Test
    void mapsProviderFailureToServiceUnavailable() {
        when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
        when(chatModel.call(org.mockito.ArgumentMatchers.any(Prompt.class)))
                .thenThrow(new RuntimeException("provider unavailable"));

        AiChatException exception = assertThrows(
                AiChatException.class,
                () -> service.chat(new AiChatRequest("你好"))
        );

        assertEquals(503, exception.getCode());
        assertEquals("AI 服务暂时不可用，请稍后重试", exception.getMessage());
    }

    private ChatResponse responseWithText(String text) {
        return new ChatResponse(List.of(
                new Generation(new AssistantMessage(text))
        ));
    }
}
