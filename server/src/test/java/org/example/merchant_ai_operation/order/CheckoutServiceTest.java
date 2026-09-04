package org.example.merchant_ai_operation.order.service;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.idempotency.mapper.IdempotentRequestMapper;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.service.CheckoutGroupService;
import org.example.merchant_ai_operation.order.service.CheckoutService;
import org.example.merchant_ai_operation.order.service.OrderService;
import org.example.merchant_ai_operation.order.vo.OrderSkuSnapshotVO;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.merchant_ai_operation.order.vo.CreateOrderVO;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.merchant_ai_operation.order.dto.CreateCheckoutRequest;
import org.example.merchant_ai_operation.order.entity.CheckoutGroup;
import org.example.merchant_ai_operation.order.vo.CreateCheckoutGroupVO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CheckoutGroupService checkoutGroupService;

    @Mock
    private ProductSkuMapper productSkuMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private IdempotentRequestMapper idempotentRequestMapper;

    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(
                productSkuMapper,
                checkoutGroupService,
                orderService,
                idempotentRequestMapper
        );

        LoginPrincipal principal =
                new LoginPrincipal(5001L, null, "CONSUMER");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGroupSnapshotsByTenant() {
        List<Long> cartItemIds = List.of(11L, 22L, 33L);

        when(productSkuMapper.selectOrderSkuSnapshots(5001L, cartItemIds))
                .thenReturn(List.of(
                        snapshot(11L, 101L),
                        snapshot(22L, 101L),
                        snapshot(33L, 202L)
                ));

        Map<Long, List<OrderSkuSnapshotVO>> result =
                checkoutService.loadGroupedSnapshots(cartItemIds);

        assertEquals(2, result.size());
        assertEquals(2, result.get(101L).size());
        assertEquals(1, result.get(202L).size());

        verify(productSkuMapper)
                .selectOrderSkuSnapshots(5001L, cartItemIds);
    }

    @Test
    void shouldRejectEmptyCartItems() {
        assertThrows(
                BizException.class,
                () -> checkoutService.loadGroupedSnapshots(List.of())
        );

        verifyNoInteractions(productSkuMapper);
    }

    @Test
    void shouldRejectMissingCartItem() {
        List<Long> cartItemIds = List.of(11L, 22L);

        when(productSkuMapper.selectOrderSkuSnapshots(5001L, cartItemIds))
                .thenReturn(List.of(snapshot(11L, 101L)));

        assertThrows(
                BizException.class,
                () -> checkoutService.loadGroupedSnapshots(cartItemIds)
        );
    }

    @Test
    void shouldCalculateTotalAcrossTenants() {
        Map<Long, List<OrderSkuSnapshotVO>> grouped =
                Map.of(
                        101L, List.of(
                                snapshot(11L, 101L),
                                snapshot(22L, 101L)
                        ),
                        202L, List.of(
                                snapshot(33L, 202L)
                        )
                );

        assertEquals(
                new BigDecimal("30.00"),
                checkoutService.calculateTotal(grouped)
        );
    }

    @Test
    void shouldCreatePendingCheckoutGroup() {
        List<Long> cartItemIds = List.of(11L, 22L, 33L);

        when(productSkuMapper.selectOrderSkuSnapshots(5001L, cartItemIds))
                .thenReturn(List.of(
                        snapshot(11L, 101L),
                        snapshot(22L, 101L),
                        snapshot(33L, 202L)
                ));

        CheckoutGroup group = new CheckoutGroup();
        group.setId(99L);
        group.setCheckoutNo("CHK20260823120000123456");
        group.setStatus("PENDING_PAYMENT");
        group.setTotalAmount(new BigDecimal("30.00"));

        when(checkoutGroupService.createPending(new BigDecimal("30.00")))
                .thenReturn(group);

        CreateCheckoutGroupVO result =
                checkoutService.createPendingCheckout(
                        new CreateCheckoutRequest(cartItemIds, 88L)
                );

        assertEquals(99L, result.checkoutGroupId());
        assertEquals("CHK20260823120000123456", result.checkoutNo());
        assertEquals("PENDING_PAYMENT", result.status());
        assertEquals(new BigDecimal("30.00"), result.totalAmount());
        assertTrue(result.orders().isEmpty());

        verify(checkoutGroupService)
                .createPending(new BigDecimal("30.00"));
    }

    @Test
    void shouldRejectMissingAddress() {
        assertThrows(
                BizException.class,
                () -> checkoutService.createPendingCheckout(
                        new CreateCheckoutRequest(List.of(11L), null)
                )
        );

        verifyNoInteractions(productSkuMapper, checkoutGroupService);
    }

    @Test
    void shouldCreateChildOrdersForEachTenant() {
        List<Long> cartItemIds = List.of(11L, 22L, 33L);

        CheckoutGroup group = new CheckoutGroup();
        group.setId(3L);
        group.setCheckoutNo("CHK20260823120000123456");
        group.setStatus("PENDING_PAYMENT");
        group.setTotalAmount(new BigDecimal("30.00"));

        when(checkoutGroupService.getMine(3L))
                .thenReturn(group);

        when(productSkuMapper.selectOrderSkuSnapshots(5001L, cartItemIds))
                .thenReturn(List.of(
                        snapshot(11L, 101L),
                        snapshot(22L, 101L),
                        snapshot(33L, 202L)
                ));

        CreateOrderVO order101 = new CreateOrderVO(
                1011L,
                "ORDER-101",
                "PENDING_PAYMENT",
                new BigDecimal("20.00"),
                null
        );

        CreateOrderVO order202 = new CreateOrderVO(
                2021L,
                "ORDER-202",
                "PENDING_PAYMENT",
                new BigDecimal("10.00"),
                null
        );

        when(orderService.createOrderVO(
                eq("checkout-key:101"),
                any(CreateOrderRequest.class),
                eq(3L)
        )).thenReturn(order101);

        when(orderService.createOrderVO(
                eq("checkout-key:202"),
                any(CreateOrderRequest.class),
                eq(3L)
        )).thenReturn(order202);

        CreateCheckoutGroupVO result =
                checkoutService.createChildOrders(
                        3L,
                        "checkout-key",
                        new CreateCheckoutRequest(cartItemIds, 88L)
                );

        assertEquals(3L, result.checkoutGroupId());
        assertEquals(2, result.orders().size());

        verify(orderService).createOrderVO(
                eq("checkout-key:101"),
                argThat(request ->
                        request.cartItemIds().equals(List.of(11L, 22L))
                                && request.addressId().equals(88L)
                ),
                eq(3L)
        );

        verify(orderService).createOrderVO(
                eq("checkout-key:202"),
                argThat(request ->
                        request.cartItemIds().equals(List.of(33L))
                                && request.addressId().equals(88L)
                ),
                eq(3L)
        );
    }

    @Test
    void shouldSubmitCheckoutInOneCall() {
        List<Long> cartItemIds = List.of(11L, 22L, 33L);
        CreateCheckoutRequest request =
                new CreateCheckoutRequest(cartItemIds, 88L);

        when(idempotentRequestMapper.selectByConsumerIdAndRequestKey(5001L, "checkout-key"))
                .thenReturn(null);
        when(idempotentRequestMapper.insert(any()))
                .thenReturn(1);
        when(idempotentRequestMapper.markCheckoutGroupSuccess(nullable(Long.class), eq(3L)))
                .thenReturn(1);

        when(productSkuMapper.selectOrderSkuSnapshots(5001L, cartItemIds))
                .thenReturn(List.of(
                        snapshot(11L, 101L),
                        snapshot(22L, 101L),
                        snapshot(33L, 202L)
                ));

        CheckoutGroup group = new CheckoutGroup();
        group.setId(3L);
        group.setCheckoutNo("CHK20260823120000123456");
        group.setStatus("PENDING_PAYMENT");
        group.setTotalAmount(new BigDecimal("30.00"));

        when(checkoutGroupService.createPending(new BigDecimal("30.00")))
                .thenReturn(group);
        when(checkoutGroupService.getMine(3L))
                .thenReturn(group);

        when(orderService.createOrderVO(
                eq("checkout-key:101"),
                any(CreateOrderRequest.class),
                eq(3L)
        )).thenReturn(new CreateOrderVO(
                1011L, "ORDER-101", "PENDING_PAYMENT",
                new BigDecimal("20.00"), null
        ));

        when(orderService.createOrderVO(
                eq("checkout-key:202"),
                any(CreateOrderRequest.class),
                eq(3L)
        )).thenReturn(new CreateOrderVO(
                2021L, "ORDER-202", "PENDING_PAYMENT",
                new BigDecimal("10.00"), null
        ));

        CreateCheckoutGroupVO result =
                checkoutService.submitCheckout("checkout-key", request);

        assertEquals(3L, result.checkoutGroupId());
        assertEquals(2, result.orders().size());

        InOrder inOrder = inOrder(checkoutGroupService, orderService);
        inOrder.verify(checkoutGroupService)
                .createPending(new BigDecimal("30.00"));
        inOrder.verify(checkoutGroupService).getMine(3L);
        inOrder.verify(orderService).createOrderVO(
                eq("checkout-key:101"),
                any(CreateOrderRequest.class),
                eq(3L)
        );
        inOrder.verify(orderService).createOrderVO(
                eq("checkout-key:202"),
                any(CreateOrderRequest.class),
                eq(3L)
        );

        verify(productSkuMapper, times(2))
                .selectOrderSkuSnapshots(5001L, cartItemIds);
    }

    @Test
    void shouldStopSubmitCheckoutWhenAnyChildOrderFails() {
        List<Long> cartItemIds = List.of(11L, 22L, 33L);
        CreateCheckoutRequest request =
                new CreateCheckoutRequest(cartItemIds, 88L);

        when(idempotentRequestMapper.selectByConsumerIdAndRequestKey(5001L, "checkout-key"))
                .thenReturn(null);
        when(idempotentRequestMapper.insert(any()))
                .thenReturn(1);

        when(productSkuMapper.selectOrderSkuSnapshots(5001L, cartItemIds))
                .thenReturn(List.of(
                        snapshot(11L, 101L),
                        snapshot(22L, 101L),
                        snapshot(33L, 202L)
                ));

        CheckoutGroup group = new CheckoutGroup();
        group.setId(3L);
        group.setCheckoutNo("CHK20260824170000123456");
        group.setStatus("PENDING_PAYMENT");
        group.setTotalAmount(new BigDecimal("30.00"));

        when(checkoutGroupService.createPending(new BigDecimal("30.00")))
                .thenReturn(group);
        when(checkoutGroupService.getMine(3L))
                .thenReturn(group);

        when(orderService.createOrderVO(
                eq("checkout-key:101"),
                any(CreateOrderRequest.class),
                eq(3L)
        )).thenReturn(new CreateOrderVO(
                1011L, "ORDER-101", "PENDING_PAYMENT",
                new BigDecimal("20.00"), null
        ));

        when(orderService.createOrderVO(
                eq("checkout-key:202"),
                any(CreateOrderRequest.class),
                eq(3L)
        )).thenThrow(new BizException(409, "商品库存不足"));

        BizException exception = assertThrows(
                BizException.class,
                () -> checkoutService.submitCheckout("checkout-key", request)
        );

        assertEquals(409, exception.getCode());
        assertEquals("商品库存不足", exception.getMessage());

        verify(orderService).createOrderVO(
                eq("checkout-key:101"),
                any(CreateOrderRequest.class),
                eq(3L)
        );
        verify(orderService).createOrderVO(
                eq("checkout-key:202"),
                any(CreateOrderRequest.class),
                eq(3L)
        );
    }

    private OrderSkuSnapshotVO snapshot(Long cartItemId, Long tenantId) {
        return new OrderSkuSnapshotVO(
                cartItemId,
                1001L,
                tenantId,
                "测试商品",
                new BigDecimal("10.00"),
                1,
                100,
                "ON_SALE",
                "ON_SALE"
        );
    }
}
