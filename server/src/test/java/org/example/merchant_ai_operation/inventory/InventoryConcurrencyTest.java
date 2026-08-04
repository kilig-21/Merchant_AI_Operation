package org.example.merchant_ai_operation.inventory;


import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class InventoryConcurrencyTest {

    private static final Long TEST_SKU_ID = 1784970220075L;
    private static final Long TEST_TENANT_ID = 1001L;

    @Autowired
    private ProductSkuMapper productSkuMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //在每次的Test结束后做测试清理工作
    @AfterEach
    void cleanUpStock() {
        resetStock();
    }

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
}
