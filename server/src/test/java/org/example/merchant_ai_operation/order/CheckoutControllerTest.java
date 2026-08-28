package org.example.merchant_ai_operation.order;

import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.order.controller.CheckoutController;
import org.example.merchant_ai_operation.order.dto.CreateCheckoutRequest;
import org.example.merchant_ai_operation.order.service.CheckoutGroupService;
import org.example.merchant_ai_operation.order.service.CheckoutService;
import org.example.merchant_ai_operation.order.vo.CreateCheckoutGroupVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.example.merchant_ai_operation.order.vo.CheckoutGroupDetailVO;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private CheckoutGroupService checkoutGroupService;

    private CheckoutController checkoutController;

    @BeforeEach
    void setUp() {
        checkoutController = new CheckoutController(
                checkoutService,
                checkoutGroupService
        );
    }

    @Test
    void shouldDelegatePrepareCheckout() {
        CreateCheckoutRequest request =
                new CreateCheckoutRequest(
                        List.of(11L, 22L),
                        88L
                );

        CreateCheckoutGroupVO groupVO =
                new CreateCheckoutGroupVO(
                        99L,
                        "CHK20260823120000123456",
                        "PENDING_PAYMENT",
                        new BigDecimal("30.00"),
                        List.of()
                );

        when(checkoutService.createPendingCheckout(request))
                .thenReturn(groupVO);

        ApiResponse<CreateCheckoutGroupVO> response =
                checkoutController.prepare(request);

        assertEquals(0, response.code());
        assertEquals(groupVO, response.data());

        verify(checkoutService)
                .createPendingCheckout(request);
    }

    @Test
    void shouldDelegateSubmitCheckout() {
        CreateCheckoutRequest request =
                new CreateCheckoutRequest(List.of(11L, 22L), 88L);

        CreateCheckoutGroupVO groupVO =
                new CreateCheckoutGroupVO(
                        3L,
                        "CHK20260824164000123456",
                        "PENDING_PAYMENT",
                        new BigDecimal("30.00"),
                        List.of()
                );

        when(checkoutService.submitCheckout("checkout-key", request))
                .thenReturn(groupVO);

        ApiResponse<CreateCheckoutGroupVO> response =
                checkoutController.submitCheckout("checkout-key", request);

        assertEquals(0, response.code());
        assertEquals(groupVO, response.data());

        verify(checkoutService)
                .submitCheckout("checkout-key", request);
    }

    @Test
    void shouldDelegateCheckoutGroupDetail() {
        CheckoutGroupDetailVO detailVO = new CheckoutGroupDetailVO(
                7L,
                "CHK20260824133000123456",
                "PENDING_PAYMENT",
                new BigDecimal("299.00"),
                LocalDateTime.of(2026, 8, 24, 13, 30),
                List.of()
        );

        when(checkoutGroupService.getMyDetail(7L))
                .thenReturn(detailVO);

        ApiResponse<CheckoutGroupDetailVO> response =
                checkoutController.getMyDetail(7L);

        assertEquals(0, response.code());
        assertEquals(detailVO, response.data());

        verify(checkoutGroupService).getMyDetail(7L);
    }
}