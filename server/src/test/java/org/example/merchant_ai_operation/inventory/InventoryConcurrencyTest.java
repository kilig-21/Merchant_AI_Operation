package org.example.merchant_ai_operation.inventory;


import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.service.OrderService;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class InventoryConcurrencyTest {

    //商品id
    private static final Long TEST_SKU_ID = 1784970220075L;
    //商家id
    private static final Long TEST_TENANT_ID = 1001L;
    //第一次测试用户id
    private static final Long FIRST_TEST_CONSUMER_ID = 4001L;
    //请求总数
    private static final int REQUEST_COUNT = 20;
    //初始化库存
    private static final int INITIAL_STOCK = 10;

    @Autowired
    private ProductSkuMapper productSkuMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private OrderService orderService;

    //在每次的Test结束后做测试清理工作
    @AfterEach
    void cleanUpStock() {
        cleanUpOrderFlowTestData();
        resetStock();
        clearLogin();
    }

    //测试20人并发锁库存
    @Test
    void concurrentLockStockShouldNotMakeStockNegative() throws Exception {
        resetStock();

        ProductSku sku = productSkuMapper.selectByIdAndTenantId(TEST_SKU_ID, TEST_TENANT_ID);

        assertNotNull(sku);
        assertEquals(10, sku.getAvailableStock());
        assertEquals(0, sku.getLockedStock());

        int requestCount = 20;
        //ExecutorService:线程池。它负责同时跑多个任务  Executors.newFixedThreadPool(20) -> 创建 20 个线程，模拟 20 个请求。
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);

        //像一个起跑线闸门。20 个线程都先等着，不让它们一个个慢慢开始。
        CountDownLatch startLatch = new CountDownLatch(1);


        //每个并发任务的结果单。因为 lockStock(...) 会返回 1 或 0，我们要把 20 个结果收回来统计。
        List<Future<Integer>> futures = new ArrayList<>();

        //创建20次任务到线程池里
        for (int i = 0; i < requestCount; i++) {
            futures.add(executorService.submit(() -> {
                startLatch.await();//每个线程到这里先等待。
                return productSkuMapper.lockStock(TEST_SKU_ID, TEST_TENANT_ID, 1);
            }));
        }

        //主线程一喊“开始”，20 个线程一起冲出去扣库存。
        startLatch.countDown();

        int successCount = 0;
        for (Future<Integer> future : futures) {
            successCount += future.get();
        }

        executorService.shutdown();

        assertEquals(10, successCount);

        ProductSku latestSku = productSkuMapper.selectByIdAndTenantId(TEST_SKU_ID, TEST_TENANT_ID);

        assertNotNull(latestSku);
        assertEquals(0, latestSku.getAvailableStock());
        assertEquals(10, latestSku.getLockedStock());
    }

    //测试20个消费者并发下单
    @Test
    void concurrentCreateOrderShouldKeepOrderInventoryAndMovementConsistent() throws Exception {
        //清理旧测试数据
        cleanUpOrderFlowTestData();
        //把测试 SKU 的库存恢复到固定状态
        resetStock();
        //创建购物车的List对象并调用准备购物车的方法
        List<Long> cartItemIds = prepareCartItems();
        //断言购物车的大小是和请求的数量是一样
        assertEquals(REQUEST_COUNT, cartItemIds.size());

        ExecutorService executorService = Executors.newFixedThreadPool(REQUEST_COUNT);
        //控制进程
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        //循环使得20个用户登录;
        for(int i = 0; i < REQUEST_COUNT; i++) {
            Long consumerId = FIRST_TEST_CONSUMER_ID + i;
            Long cartItemId = 9000000000000L + i;

            futures.add(executorService.submit(() -> {
                mockConsumerLogin(consumerId);
                startLatch.await();

                try {
                    orderService.createOrderVO(new CreateOrderRequest(List.of(cartItemId)));
                    return true;
                } catch (Exception ex) {
                    return false;
                } finally {
                    clearLogin();
                }
            }));
        }

        startLatch.countDown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (Boolean.TRUE.equals(future.get())) {
                successCount++;
            }
        }

        executorService.shutdown();

        assertEquals(INITIAL_STOCK, successCount);

        //查数据库里的真实数据,检测是不是真实落在了数据库里
        Integer orderCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM commerce_order
                WHERE consumer_id >= ?
                  AND consumer_id < ?
                """, Integer.class, FIRST_TEST_CONSUMER_ID, FIRST_TEST_CONSUMER_ID + REQUEST_COUNT);
        //断言
        assertEquals(INITIAL_STOCK, orderCount);


        //双重验证:创建了10个订单也就创建了10条锁库存流水
        Integer lockMovementCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM inventory_movement im
                JOIN commerce_order co ON im.business_no = co.order_no
                WHERE co.consumer_id >= ?
                  AND co.consumer_id < ?
                  AND im.business_type = 'ORDER_LOCK'
                """, Integer.class, FIRST_TEST_CONSUMER_ID, FIRST_TEST_CONSUMER_ID + REQUEST_COUNT);
        //断言
        assertEquals(INITIAL_STOCK, lockMovementCount);

        //最终库存断言
        ProductSku latestSku = productSkuMapper.selectByIdAndTenantId(TEST_SKU_ID, TEST_TENANT_ID);

        assertNotNull(latestSku);
        assertEquals(0, latestSku.getAvailableStock());
        assertEquals(INITIAL_STOCK, latestSku.getLockedStock());

        Integer remainingCartItemCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM cart_item
                WHERE consumer_id >= ?
                  AND consumer_id < ?
                """, Integer.class, FIRST_TEST_CONSUMER_ID, FIRST_TEST_CONSUMER_ID + REQUEST_COUNT);

        assertEquals(REQUEST_COUNT - INITIAL_STOCK, remainingCartItemCount);

    }

    //重置库存sql方法
    private void resetStock() {
        int rows = jdbcTemplate.update("""
            UPDATE product_sku
            SET available_stock = 10,
                locked_stock = 0,
                version = version + 1
            WHERE id = ?
              AND tenant_id = ?
            """, TEST_SKU_ID, TEST_TENANT_ID);

        assertEquals(1, rows);
    }

    //批量加入购物车
    private int insertCartItem(Long cartItemId, Long consumerId) {
        return jdbcTemplate.update("""
        INSERT INTO cart_item (
            id,
            consumer_id,
            sku_id,
            quantity
        )
        VALUES (?, ?, ?, 1)
        """, cartItemId, consumerId, TEST_SKU_ID);
    }

    //给测试线程放一个“当前用户”
    private void mockConsumerLogin(Long consumerId){
        LoginPrincipal principal = new LoginPrincipal(consumerId, null, "CONSUMER");

        //把这个登录人包装成 Spring Security 认识的“认证对象”。
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        //把这个“认证对象”塞进当前测试线程的 Spring Security 上下文里。
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    //登陆的的信息用完后消除
    private void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    //准备购物车Items
    private List<Long> prepareCartItems() {
        List<Long> cartItemIds = new ArrayList<>();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            Long consumerId = FIRST_TEST_CONSUMER_ID + i;
            Long cartItemId = 9000000000000L + i;


            assertEquals(1, insertCartItem(cartItemId, consumerId));
            cartItemIds.add(cartItemId);
        }

        return cartItemIds;
    }

    //每次测试开始前，要先把上一次留下的测试购物车、测试订单、测试流水清掉
    private void cleanUpOrderFlowTestData() {
        //删库存流水
        jdbcTemplate.update("""
        DELETE FROM inventory_movement
        WHERE business_no IN (
            SELECT order_no
            FROM commerce_order
            WHERE consumer_id >= ?
              AND consumer_id < ?
        )
        """, FIRST_TEST_CONSUMER_ID, FIRST_TEST_CONSUMER_ID + REQUEST_COUNT);

        //删订单明细
        jdbcTemplate.update("""
        DELETE FROM commerce_order_item
        WHERE order_id IN (
            SELECT id
            FROM commerce_order
            WHERE consumer_id >= ?
              AND consumer_id < ?
        )
        """, FIRST_TEST_CONSUMER_ID, FIRST_TEST_CONSUMER_ID + REQUEST_COUNT);

        //删订单主表
        jdbcTemplate.update("""
        DELETE FROM commerce_order
        WHERE consumer_id >= ?
          AND consumer_id < ?
        """, FIRST_TEST_CONSUMER_ID, FIRST_TEST_CONSUMER_ID + REQUEST_COUNT);

        //删购物车
        jdbcTemplate.update("""
        DELETE FROM cart_item
        WHERE consumer_id >= ?
          AND consumer_id < ?
        """, FIRST_TEST_CONSUMER_ID, FIRST_TEST_CONSUMER_ID + REQUEST_COUNT);
    }



}
