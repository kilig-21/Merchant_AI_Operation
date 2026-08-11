package org.example.merchant_ai_operation.order;


import org.example.merchant_ai_operation.order.service.OrderCloseService;
import org.example.merchant_ai_operation.order.service.OrderService;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OrderPaymentCloseConcurrencyTest {
    private static final Long TEST_ORDER_ID = 9300000000000L;
    private static final Long TEST_ORDER_ITEM_ID = 9300000000001L;
    private static final Long TEST_SKU_ID = 1784970220075L;
    private static final Long TEST_TENANT_ID = 1001L;
    private static final Long TEST_CONSUMER_ID = 6001L;
    private static final String TEST_ORDER_NO = "TEST-PAY-CLOSE-001";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderCloseService orderCloseService;

    //@RepeatedTest(10)重复测试10次
    @RepeatedTest(10)
    void paymentAndCloseShouldHaveOnlyOneWinner() throws Exception {
        PaymentCloseResult result = runPaymentAndClose(true);

        assertEquals("PAY_REJECTED", result.payResult());
        assertEquals("CLOSE_FINISHED", result.closeResult());

        Integer closeMovementCount = assertClosedOrderAndGetCloseMovementCount(result.orderId());

        assertEquals(1, closeMovementCount);
    }

    @RepeatedTest(10)
    void paymentShouldWinBeforeOrderExpiry() throws Exception {
        PaymentCloseResult result = runPaymentAndClose(false);

        assertEquals("PAID", result.payResult());
        assertEquals("CLOSE_FINISHED", result.closeResult());

        assertPaidOrderAndInventoryConsistent(result.orderId());
    }



    @AfterEach
    void cleanUp() {
        clearLogin();

        jdbcTemplate.update(
                "DELETE FROM inventory_movement WHERE business_no = ?",
                TEST_ORDER_NO
        );

        jdbcTemplate.update(
                "DELETE FROM commerce_order_item WHERE order_id = ?",
                TEST_ORDER_ID
        );

        jdbcTemplate.update(
                "DELETE FROM commerce_order WHERE id = ?",
                TEST_ORDER_ID
        );

        jdbcTemplate.update("""
            UPDATE product_sku
            SET available_stock = 10,
                locked_stock = 0,
                version = version + 1
            WHERE id = ?
              AND tenant_id = ?
            """,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );
    }




    //记录支付线程和关单线程的执行结果
    private record PaymentCloseResult(
            Long orderId,
            String payResult,
            String closeResult
    ) {}

    //准备数据
    private Long preparePendingOrder(boolean expired)  {
        jdbcTemplate.update(
                "DELETE FROM inventory_movement WHERE business_no = ?",
                TEST_ORDER_NO
        );

        jdbcTemplate.update(
                "DELETE FROM commerce_order_item WHERE order_id = ?",
                TEST_ORDER_ID
        );

        jdbcTemplate.update(
                "DELETE FROM commerce_order WHERE id = ?",
                TEST_ORDER_ID
        );

        int stockRows = jdbcTemplate.update("""
            UPDATE product_sku
            SET available_stock = 19,
                locked_stock = 1,
                version = version + 1
            WHERE id = ?
              AND tenant_id = ?
            """,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );

        assertEquals(1, stockRows);

        LocalDateTime expireAt = expired
                ? LocalDateTime.now().minusSeconds(2)
                : LocalDateTime.now().plusMinutes(10);

        int orderRows = jdbcTemplate.update("""
            INSERT INTO commerce_order (
                id, order_no, tenant_id, consumer_id,
                status, total_amount, expire_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
                TEST_ORDER_ID,
                TEST_ORDER_NO,
                TEST_TENANT_ID,
                TEST_CONSUMER_ID,
                "PENDING_PAYMENT",
                new BigDecimal("199.00"),
                expireAt
        );

        assertEquals(1, orderRows);

        int itemRows = jdbcTemplate.update("""
            INSERT INTO commerce_order_item (
                id, order_id, sku_id, sku_name_snapshot,
                sale_price, quantity
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                TEST_ORDER_ITEM_ID,
                TEST_ORDER_ID,
                TEST_SKU_ID,
                "并发测试商品",
                new BigDecimal("199.00"),
                1
        );

        assertEquals(1, itemRows);

        return TEST_ORDER_ID;
    }

    //模拟消费者登录
    private void mockConsumerLogin() {
        LoginPrincipal principal = new LoginPrincipal(TEST_CONSUMER_ID, null, "CONSUMER");

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    //清理登录信息
    private void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    //sql查询
    private Integer assertClosedOrderAndGetCloseMovementCount(Long orderId) {
        String finalStatus = jdbcTemplate.queryForObject("""
        SELECT status
        FROM commerce_order
        WHERE id = ?
        """, String.class, orderId);

        assertEquals("CLOSED", finalStatus);

        Integer availableStock = jdbcTemplate.queryForObject("""
        SELECT available_stock
        FROM product_sku
        WHERE id = ?
          AND tenant_id = ?
        """, Integer.class, TEST_SKU_ID, TEST_TENANT_ID);

        Integer lockedStock = jdbcTemplate.queryForObject("""
        SELECT locked_stock
        FROM product_sku
        WHERE id = ?
          AND tenant_id = ?
        """, Integer.class, TEST_SKU_ID, TEST_TENANT_ID);

        assertEquals(20, availableStock);
        assertEquals(0, lockedStock);

        return jdbcTemplate.queryForObject("""
        SELECT COUNT(1)
        FROM inventory_movement
        WHERE business_type = 'ORDER_CLOSE'
          AND business_no = ?
          AND sku_id = ?
        """,
                Integer.class,
                TEST_ORDER_NO,
                TEST_SKU_ID
        );
    }

    //擦入方法
    private void assertPaidOrderAndInventoryConsistent(Long orderId) {
        String finalStatus = jdbcTemplate.queryForObject("""
        SELECT status
        FROM commerce_order
        WHERE id = ?
        """, String.class, orderId);

        assertEquals("PAID", finalStatus);

        Integer availableStock = jdbcTemplate.queryForObject("""
        SELECT available_stock
        FROM product_sku
        WHERE id = ?
          AND tenant_id = ?
        """, Integer.class, TEST_SKU_ID, TEST_TENANT_ID);

        Integer lockedStock = jdbcTemplate.queryForObject("""
        SELECT locked_stock
        FROM product_sku
        WHERE id = ?
          AND tenant_id = ?
        """, Integer.class, TEST_SKU_ID, TEST_TENANT_ID);

        Integer paidMovementCount = jdbcTemplate.queryForObject("""
        SELECT COUNT(1)
        FROM inventory_movement
        WHERE business_type = 'ORDER_PAID'
          AND business_no = ?
          AND sku_id = ?
        """,
                Integer.class,
                TEST_ORDER_NO,
                TEST_SKU_ID
        );

        assertEquals(1, paidMovementCount);
        assertEquals(19, availableStock);
        assertEquals(0, lockedStock);

    }

    //运行是支付和关闭的并发
    private PaymentCloseResult runPaymentAndClose(boolean expired) throws Exception {
        Long orderId = preparePendingOrder(expired);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Future<String> payFuture = pool.submit(() -> {
            mockConsumerLogin();
            startLatch.await();
            try {
                orderService.mockPay(orderId);
                return "PAID";
            } catch (Exception ex) {
                return "PAY_REJECTED";
            } finally {
                clearLogin();
            }
        });

        Future<String> closeFuture = pool.submit(() -> {
            startLatch.await();
            orderCloseService.closeExpiredOrder(orderId);
            return "CLOSE_FINISHED";
        });

        startLatch.countDown();

        try {
            String payResult = payFuture.get();
            String closeResult = closeFuture.get();

            return new PaymentCloseResult(orderId, payResult, closeResult);
        } finally {
            pool.shutdown();
        }
    }
}
