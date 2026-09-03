package org.example.merchant_ai_operation.order;

import org.example.merchant_ai_operation.order.entity.CheckoutGroup;
import org.example.merchant_ai_operation.order.mapper.CheckoutGroupMapper;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderItemMapper;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;
import org.example.merchant_ai_operation.order.service.CheckoutGroupService;
import org.example.merchant_ai_operation.order.service.CommerceOrderAddressService;
import org.example.merchant_ai_operation.order.vo.OrderAddressSnapshotVO;
import org.example.merchant_ai_operation.order.vo.OrderItemVO;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.order.entity.CommerceOrder;
import org.example.merchant_ai_operation.order.vo.CheckoutGroupDetailVO;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutGroupServiceTest {

    @Mock
    private CommerceOrderItemMapper commerceOrderItemMapper;

    @Mock
    private CommerceOrderAddressService commerceOrderAddressService;

    @Mock
    private CheckoutGroupMapper checkoutGroupMapper;

    @Mock
    private CommerceOrderMapper commerceOrderMapper;

    private CheckoutGroupService checkoutGroupService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-23T04:00:00Z"),
                ZoneId.of("Asia/Shanghai")
        );

        checkoutGroupService = new CheckoutGroupService(
                checkoutGroupMapper,
                clock,
                commerceOrderMapper,
                commerceOrderItemMapper,
                commerceOrderAddressService
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
    void createPendingShouldBuildConsumerCheckoutGroup() {
        when(checkoutGroupMapper.insert(any(CheckoutGroup.class)))
                .thenAnswer(invocation -> {
                    CheckoutGroup group = invocation.getArgument(0);
                    group.setId(1L);
                    return 1;
                });

        CheckoutGroup result =
                checkoutGroupService.createPending(new BigDecimal("199.00"));

        assertEquals(1L, result.getId());
        assertEquals(5001L, result.getConsumerId());
        assertEquals("PENDING_PAYMENT", result.getStatus());
        assertEquals(new BigDecimal("199.00"), result.getTotalAmount());
        assertTrue(result.getCheckoutNo().startsWith("CHK20260823120000"));

        ArgumentCaptor<CheckoutGroup> captor =
                ArgumentCaptor.forClass(CheckoutGroup.class);

        verify(checkoutGroupMapper).insert(captor.capture());
        assertEquals(5001L, captor.getValue().getConsumerId());
    }

    @Test
    void getMineShouldQueryWithCurrentConsumerId() {
        CheckoutGroup group = new CheckoutGroup();
        group.setId(7L);
        group.setConsumerId(5001L);

        when(checkoutGroupMapper.selectByIdAndConsumerId(7L, 5001L))
                .thenReturn(group);

        CheckoutGroup result = checkoutGroupService.getMine(7L);

        assertEquals(7L, result.getId());
        verify(checkoutGroupMapper)
                .selectByIdAndConsumerId(7L, 5001L);
    }

    @Test
    void createPendingShouldRejectNegativeAmount() {
        assertThrows(
                BizException.class,
                () -> checkoutGroupService.createPending(new BigDecimal("-1.00"))
        );

        verifyNoInteractions(checkoutGroupMapper);
    }

    @Test
    void getMyDetailShouldReturnMyGroupAndChildOrders() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 24, 13, 30);

        CheckoutGroup group = new CheckoutGroup();
        group.setId(7L);
        group.setConsumerId(5001L);
        group.setCheckoutNo("CHK20260824133000123456");
        group.setStatus("PENDING_PAYMENT");
        group.setTotalAmount(new BigDecimal("299.00"));
        group.setCreatedAt(createdAt);

        CommerceOrder childOrder = new CommerceOrder();
        childOrder.setId(701L);
        childOrder.setCheckoutGroupId(7L);
        childOrder.setConsumerId(5001L);
        childOrder.setTenantId(1001L);
        childOrder.setOrderNo("ORDER-701");
        childOrder.setStatus("PENDING_PAYMENT");
        childOrder.setTotalAmount(new BigDecimal("299.00"));
        childOrder.setExpireAt(createdAt.plusMinutes(30));
        childOrder.setCreatedAt(createdAt);

        OrderItemVO item = new OrderItemVO(
                1L, 1001L, "测试 SKU", new BigDecimal("299.00"), 1
        );
        OrderAddressSnapshotVO address = new OrderAddressSnapshotVO(
                "张三", "13800138000", "四川省", "成都市", "武侯区", "测试街道 1 号"
        );

        when(checkoutGroupMapper.selectByIdAndConsumerId(7L, 5001L))
                .thenReturn(group);
        when(commerceOrderMapper.selectByCheckoutGroupIdAndConsumerId(7L, 5001L))
                .thenReturn(List.of(childOrder));

        CheckoutGroupDetailVO result = checkoutGroupService.getMyDetail(7L);

        assertEquals(7L, result.checkoutGroupId());
        assertEquals("CHK20260824133000123456", result.checkoutNo());
        assertEquals(1, result.orders().size());
        assertEquals(701L, result.orders().get(0).id());
        assertEquals(7L, result.orders().get(0).checkoutGroupId());
        assertEquals(1, result.orders().get(0).items().size());
        assertEquals("测试 SKU", result.orders().get(0).items().getFirst().skuNameSnapshot());
        assertEquals("张三", result.orders().get(0).shippingAddress().receiverName());

        verify(commerceOrderItemMapper).selectItemVOByOrderId(701L);
        verify(commerceOrderAddressService).getSnapshot(701L);
        verify(checkoutGroupMapper).selectByIdAndConsumerId(7L, 5001L);
        verify(commerceOrderMapper)
                .selectByCheckoutGroupIdAndConsumerId(7L, 5001L);
    }

    @Test
    void getMineShouldRejectMissingCheckoutGroup() {
        when(checkoutGroupMapper.selectByIdAndConsumerId(99L, 5001L))
                .thenReturn(null);

        assertThrows(
                BizException.class,
                () -> checkoutGroupService.getMine(99L)
        );
    }
}