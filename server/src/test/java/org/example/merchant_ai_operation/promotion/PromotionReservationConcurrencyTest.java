package org.example.merchant_ai_operation.promotion;

import org.example.merchant_ai_operation.promotion.dto.PromotionReservationResult;
import org.example.merchant_ai_operation.promotion.entity.PromotionActivity;
import org.example.merchant_ai_operation.promotion.entity.PromotionItem;
import org.example.merchant_ai_operation.promotion.mapper.PromotionActivityMapper;
import org.example.merchant_ai_operation.promotion.mapper.PromotionItemMapper;
import org.example.merchant_ai_operation.outbox.service.OutboxPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.promotion.dto.ReservePromotionRequest;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.example.merchant_ai_operation.order.MutableTestClock;
import org.example.merchant_ai_operation.promotion.service.PromotionRedisPreheatService;
import org.example.merchant_ai_operation.promotion.service.PromotionReservationService;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Instant;
import java.time.ZoneId;


@SpringBootTest
@Import(PromotionReservationConcurrencyTest.TestClockConfig.class)
public class PromotionReservationConcurrencyTest {

    private static final Long TEST_TENANT_ID = 1001L;
    private static final int REQUEST_COUNT = 20;
    private static final int ACTIVITY_STOCK = 10;
    private static final Long TEST_SKU_ID = 1784970220075L;
    private static final Long TEST_MERCHANT_ID = 2L;
    private static final Long FIRST_TEST_CONSUMER_ID = 7101L;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PromotionReservationService promotionReservationService;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private PromotionActivityMapper promotionActivityMapper;

    @Autowired
    private PromotionItemMapper promotionItemMapper;

    @Autowired
    private PromotionRedisPreheatService promotionRedisPreheatService;

    @Autowired
    private MutableTestClock testClock;

    private Long testActivityId;
    private Long testActivityItemId;

    @Test
    void shouldLoadPromotionConcurrencyTestContext(){
        assertNotNull(jdbcTemplate);
        assertNotNull(promotionReservationService);
        assertNotNull(promotionRedisPreheatService);
        assertNotNull(testClock);
    }

    @Test
    void concurrentReservationsShouldNotOversell() throws Exception {
        ExecutorService executorService =
                Executors.newFixedThreadPool(REQUEST_COUNT);

        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < REQUEST_COUNT; i++) {
                Long consumerId = FIRST_TEST_CONSUMER_ID + i;

                futures.add(executorService.submit(() -> {
                    mockConsumerLogin(consumerId);
                    startLatch.await();

                    try {
                        promotionReservationService.reserve(
                                new ReservePromotionRequest(
                                        testActivityItemId,
                                        1,
                                        "step24-concurrency-" + consumerId
                                )
                        );
                        return true;
                    } catch (BizException exception) {
                        if (exception.getCode() == 409) {
                            return false;
                        }
                        throw exception;
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

            assertEquals(ACTIVITY_STOCK, successCount);

            assertEquals(
                    "0",
                    stringRedisTemplate.opsForValue()
                            .get(stockKey(testActivityItemId))
            );

        Integer reservationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM promotion_reservations
                WHERE activity_id = ?
                """, Integer.class, testActivityId);

        assertEquals(ACTIVITY_STOCK, reservationCount);

        outboxPublisher.publishPendingEvents();

        waitForPromotionOrdersCreated();

        Integer orderCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM commerce_order co
                JOIN promotion_reservations pr
                  ON pr.order_id = co.id
                WHERE pr.activity_id = ?
                """, Integer.class, testActivityId);

        assertEquals(ACTIVITY_STOCK, orderCount);

        Integer orderCreatedReservationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM promotion_reservations
                WHERE activity_id = ?
                  AND status = 'ORDER_CREATED'
                """, Integer.class, testActivityId);

        assertEquals(ACTIVITY_STOCK, orderCreatedReservationCount);

        Integer mysqlAvailableStock = jdbcTemplate.queryForObject("""
                SELECT stock_available
                FROM promotion_items
                WHERE id = ?
                """, Integer.class, testActivityItemId);

        assertEquals(0, mysqlAvailableStock);

        Integer publishedOutboxCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM outbox_events
                WHERE aggregate_type = 'PROMOTION_RESERVATION'
                  AND event_type = 'PROMOTION_ORDER_CREATE'
                  AND status = 'PUBLISHED'
                  AND aggregate_id IN (
                      SELECT id
                      FROM promotion_reservations
                      WHERE activity_id = ?
                  )
                """, Integer.class, testActivityId);

        assertEquals(ACTIVITY_STOCK, publishedOutboxCount);
        } finally {
            executorService.shutdown();

            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
    }

    @Test
    void orderCreationFailureShouldCompensateReservationAndRedisStock() throws Exception {
        Long missingSkuId = 999999999999L;
        Long consumerId = FIRST_TEST_CONSUMER_ID;

        jdbcTemplate.update("""
            UPDATE promotion_items
            SET sku_id = ?
            WHERE id = ?
            """, missingSkuId, testActivityItemId);

        PromotionReservationResult result;

        mockConsumerLogin(consumerId);
        try {
            result = promotionReservationService.reserve(
                    new ReservePromotionRequest(
                            testActivityItemId,
                            1,
                            "step24-compensation-" + consumerId
                    )
            );
        } finally {
            clearLogin();
        }

        assertEquals(1, result.code());

        outboxPublisher.publishPendingEvents();

        waitForCompensationCompleted(result.reservationId());

        String reservationStatus = jdbcTemplate.queryForObject("""
            SELECT status
            FROM promotion_reservations
            WHERE reservation_id = ?
            """, String.class, result.reservationId());

        assertEquals("COMPENSATED", reservationStatus);

        Integer compensationCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(1)
            FROM promotion_compensation_records
            WHERE reservation_id = ?
              AND compensation_type = 'ORDER_CREATE_FAILURE'
              AND status = 'COMPLETED'
            """, Integer.class, result.reservationId());

        assertEquals(1, compensationCount);

        Integer orderCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(1)
            FROM commerce_order co
            JOIN promotion_reservations pr
              ON pr.order_id = co.id
            WHERE pr.reservation_id = ?
            """, Integer.class, result.reservationId());

        assertEquals(0, orderCount);

        assertEquals(
                String.valueOf(ACTIVITY_STOCK),
                stringRedisTemplate.opsForValue()
                        .get(stockKey(testActivityItemId))
        );

        assertEquals(
                "0",
                stringRedisTemplate.opsForValue()
                        .get(userQuantityKey(testActivityItemId, consumerId))
        );
    }

    //准备促销数据
    @BeforeEach
    void setUpTestPromotion() {
        LocalDateTime now = LocalDateTime.now(testClock);

        PromotionActivity activity = new PromotionActivity();
        activity.setTenantId(TEST_TENANT_ID);
        activity.setName("步骤24并发测试-" + UUID.randomUUID());
        activity.setStartAt(now.minusMinutes(1));
        activity.setEndAt(now.plusMinutes(30));
        activity.setStatus("SCHEDULED");

        assertEquals(1, promotionActivityMapper.insert(activity));
        assertNotNull(activity.getId());
        testActivityId = activity.getId();

        PromotionItem item = new PromotionItem();
        item.setActivityId(testActivityId);
        item.setTenantId(TEST_TENANT_ID);
        item.setSkuId(TEST_SKU_ID);
        item.setActivityPrice(new BigDecimal("99.00"));
        item.setStockTotal(ACTIVITY_STOCK);
        item.setStockAvailable(ACTIVITY_STOCK);
        item.setLimitPerUser(1);

        assertEquals(1, promotionItemMapper.insert(item));
        assertNotNull(item.getId());
        testActivityItemId = item.getId();

        mockMerchantLogin();
        try {
            promotionRedisPreheatService.preheat(testActivityId);
        } finally {
            clearLogin();
        }

        assertEquals(
                String.valueOf(ACTIVITY_STOCK),
                stringRedisTemplate.opsForValue().get(stockKey(testActivityItemId))
        );
    }

    //清理促销数据
    @AfterEach
    void cleanUpTestPromotion() {
        clearLogin();

        if (testActivityItemId != null) {
            Set<String> redisKeys = stringRedisTemplate.keys(
                    "promotion:item:{" + testActivityItemId + "}:*"
            );

            if (redisKeys != null && !redisKeys.isEmpty()) {
                stringRedisTemplate.delete(redisKeys);
            }
        }

        if (testActivityId == null) {
            return;
        }

        jdbcTemplate.update("""
            DELETE FROM outbox_events
            WHERE aggregate_type = 'PROMOTION_RESERVATION'
              AND aggregate_id IN (
                  SELECT id
                  FROM promotion_reservations
                  WHERE activity_id = ?
              )
            """, testActivityId);

        jdbcTemplate.update("""
            DELETE FROM promotion_compensation_records
            WHERE activity_item_id IN (
                SELECT id
                FROM promotion_items
                WHERE activity_id = ?
            )
            """, testActivityId);

        jdbcTemplate.update("""
            DELETE oi
            FROM commerce_order_item oi
            JOIN promotion_reservations pr
              ON pr.order_id = oi.order_id
            WHERE pr.activity_id = ?
            """, testActivityId);

        jdbcTemplate.update("""
            DELETE co
            FROM commerce_order co
            JOIN promotion_reservations pr
              ON pr.order_id = co.id
            WHERE pr.activity_id = ?
            """, testActivityId);

        jdbcTemplate.update("""
            DELETE FROM promotion_reservations
            WHERE activity_id = ?
            """, testActivityId);

        jdbcTemplate.update("""
            DELETE FROM promotion_items
            WHERE activity_id = ?
            """, testActivityId);

        jdbcTemplate.update("""
            DELETE FROM promotion_activities
            WHERE id = ?
            """, testActivityId);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestClockConfig{

        @Bean
        @Primary
        MutableTestClock testClock() {
            return new MutableTestClock(
                    Instant.parse("2026-08-19T10:00:00Z"),
                    ZoneId.of("Asia/Shanghai")
            );
        }

    }

    private void mockConsumerLogin(Long consumerId) {
        LoginPrincipal principal = new LoginPrincipal(
                consumerId,
                null,
                "CONSUMER"
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                )
        );
    }

    private void mockMerchantLogin() {
        LoginPrincipal principal = new LoginPrincipal(
                TEST_MERCHANT_ID,
                TEST_TENANT_ID,
                "MERCHANT_ADMIN"
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                )
        );
    }

    private void waitForPromotionOrdersCreated() throws InterruptedException {
        long deadlineNanos = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(10);

        while (System.nanoTime() < deadlineNanos) {
            Integer orderCreatedCount = jdbcTemplate.queryForObject("""
                    SELECT COUNT(1)
                    FROM promotion_reservations
                    WHERE activity_id = ?
                      AND status = 'ORDER_CREATED'
                    """, Integer.class, testActivityId);

            if (orderCreatedCount != null
                    && orderCreatedCount == ACTIVITY_STOCK) {
                return;
            }

            Thread.sleep(100);
        }

        fail("等待 10 秒后，促销订单仍未全部创建完成");
    }

    private void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    private void waitForCompensationCompleted(String reservationId) throws InterruptedException {
        long deadlineNanos = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(10);

        while (System.nanoTime() < deadlineNanos) {
            Integer completedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM promotion_compensation_records
                WHERE reservation_id = ?
                  AND compensation_type = 'ORDER_CREATE_FAILURE'
                  AND status = 'COMPLETED'
                """, Integer.class, reservationId);

            if (completedCount != null && completedCount == 1) {
                return;
            }

            Thread.sleep(100);
        }

        fail("等待 10 秒后，促销补偿仍未完成");
    }

    private String stockKey(Long itemId) {
        return "promotion:item:{" + itemId + "}:stock:v1";
    }

    private String rulesKey(Long itemId) {
        return "promotion:item:{" + itemId + "}:rules:v1";
    }

    private String userQuantityKey(Long itemId, Long consumerId) {
        return "promotion:item:{" + itemId
                + "}:user:" + consumerId + ":v1";
    }





}
