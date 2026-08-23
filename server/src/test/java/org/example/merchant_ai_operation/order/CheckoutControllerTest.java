package org.example.merchant_ai_operation.order;

import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.order.controller.CheckoutController;
import org.example.merchant_ai_operation.order.dto.CreateCheckoutRequest;
import org.example.merchant_ai_operation.order.service.CheckoutService;
import org.example.merchant_ai_operation.order.vo.CreateCheckoutGroupVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    private CheckoutController checkoutController;

    @BeforeEach
    void setUp() {
        checkoutController = new CheckoutController(checkoutService);
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
}