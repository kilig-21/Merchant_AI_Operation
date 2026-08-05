package org.example.merchant_ai_operation.order;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.service.OrderService;
import org.example.merchant_ai_operation.order.vo.CreateOrderVO;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderIdempotencyTest {

    private static final Long TEST_SKU_ID = 1784970220075L;
    private static final Long TEST_TENANT_ID = 1001L;
    private static final Long TEST_CONSUMER_ID = 5001L;
    private static final Long TEST_CART_ITEM_ID = 9100000000000L;
    private static final Long ANOTHER_TEST_CART_ITEM_ID = 9100000000001L;
    private static final String IDEMPOTENCY_KEY = "test-idempotent-order-001";
    private static final int REQUEST_COUNT = 20;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderService orderService;

    //清理原有的残留
    @AfterEach
    void cleanUp() {
        clearTestData();
        clearLogin();
    }

    //测试统一key和统一参数应返回相同
    @Test
    void sameIdempotencyKeyShouldReturnSameOrder(){
        //防止上一次没清理完而残留的
        clearTestData();
        prepareCartItem(TEST_CART_ITEM_ID);
        mockConsumerLogin();

        //第一次请求，应该真正创建订单
        CreateOrderVO first = orderService.createOrderVO(
                IDEMPOTENCY_KEY,
                new CreateOrderRequest(List.of(TEST_CART_ITEM_ID))
        );

        //第二次请求，模拟用户双击/网络重试，应该返回 first 的同一个订单
        CreateOrderVO second = orderService.createOrderVO(
                IDEMPOTENCY_KEY,
                new CreateOrderRequest(List.of(TEST_CART_ITEM_ID))
        );

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.orderId(), second.orderId());
        assertEquals(first.orderNo(), second.orderNo());

    }

    //测试同一个key和不同参数应报错
    @Test
    void sameIdempotencyKeyWithDifferentRequestShouldConflict() {
        clearTestData();
        prepareCartItem(TEST_CART_ITEM_ID);
        mockConsumerLogin();

        orderService.createOrderVO(
                IDEMPOTENCY_KEY,
                new CreateOrderRequest(List.of(TEST_CART_ITEM_ID))
        );

        BizException ex = assertThrows(
                BizException.class,
                () -> orderService.createOrderVO(
                        IDEMPOTENCY_KEY,
                        new CreateOrderRequest(List.of(ANOTHER_TEST_CART_ITEM_ID))
                )
        );

        assertEquals(409, ex.getCode());
        assertEquals("同一个 Idempotency-Key 不能用于不同下单参数", ex.getMessage());
    }

    //并发二十个请求
    @Test
    void concurrentSameIdempotencyKeyShouldCreateOnlyOneOrder() throws Exception {
        clearTestData();
        prepareCartItem(TEST_CART_ITEM_ID);

        ExecutorService executorService = Executors.newFixedThreadPool(REQUEST_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            futures.add(executorService.submit(() -> {
                mockConsumerLogin();
                startLatch.await();

                try {
                    CreateOrderVO order = orderService.createOrderVO(
                            IDEMPOTENCY_KEY,
                            new CreateOrderRequest(List.of(TEST_CART_ITEM_ID))
                    );
                    return order.orderId();
                } catch (BizException ex) {
                    return null;
                } finally {
                    clearLogin();
                }
            }));
        }
        startLatch.countDown();


        List<Long> successOrderIds = new ArrayList<>();
        int rejectedCount = 0;

        for (Future<Long> future : futures) {
            Long orderId = future.get();
            if (orderId == null) {
                rejectedCount++;
            } else {
                successOrderIds.add(orderId);
            }
        }

        executorService.shutdown();

        int finishedCount = successOrderIds.size() + rejectedCount;

        assertEquals(REQUEST_COUNT, finishedCount, "所有并发请求都应该有明确结果");
        assertFalse(successOrderIds.isEmpty());
        assertEquals(1, successOrderIds.stream().distinct().count());

        Integer orderCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(1)
            FROM commerce_order
            WHERE consumer_id = ?
            """, Integer.class, TEST_CONSUMER_ID);

        assertEquals(1, orderCount);

        Integer idempotentCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(1)
            FROM idempotent_request
            WHERE consumer_id = ?
              AND request_key = ?
              AND status = 'SUCCESS'
            """, Integer.class, TEST_CONSUMER_ID, IDEMPOTENCY_KEY);

        assertEquals(1, idempotentCount);
    }



    //准备购物车项
    private void prepareCartItem(Long cartItemId){
        int rows = jdbcTemplate.update("""
            INSERT INTO cart_item (
                id,
                consumer_id,
                sku_id,
                quantity
            )
            VALUES (?, ?, ?, 1)
            """, cartItemId, TEST_CONSUMER_ID, TEST_SKU_ID);

        if (rows != 1) {
            throw new IllegalStateException("准备购物车测试数据失败");
        }
    }

    //模拟消费者登录
    private void mockConsumerLogin(){
        //做一个登录信息
        LoginPrincipal principal = new LoginPrincipal(TEST_CONSUMER_ID, null, "CONSUMER");

        //新建一个token
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        //加到线程中去
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    //登录清理
    private void clearLogin(){
        //直接清理上下文
        SecurityContextHolder.clearContext();
    }

    //清理的sql语句
    private void clearTestData() {
        jdbcTemplate.update("""
            DELETE FROM idempotent_request
            WHERE consumer_id = ?
            """, TEST_CONSUMER_ID);

        jdbcTemplate.update("""
            DELETE FROM inventory_movement
            WHERE business_no IN (
                SELECT order_no
                FROM commerce_order
                WHERE consumer_id = ?
            )
            """, TEST_CONSUMER_ID);

        jdbcTemplate.update("""
            DELETE FROM commerce_order_item
            WHERE order_id IN (
                SELECT id
                FROM commerce_order
                WHERE consumer_id = ?
            )
            """, TEST_CONSUMER_ID);

        jdbcTemplate.update("""
            DELETE FROM commerce_order
            WHERE consumer_id = ?
            """, TEST_CONSUMER_ID);

        jdbcTemplate.update("""
            DELETE FROM cart_item
            WHERE consumer_id = ?
            """, TEST_CONSUMER_ID);

        jdbcTemplate.update("""
            UPDATE product_sku
            SET available_stock = 10,
                locked_stock = 0,
                version = version + 1
            WHERE id = ?
              AND tenant_id = ?
            """, TEST_SKU_ID, TEST_TENANT_ID);
    }

}