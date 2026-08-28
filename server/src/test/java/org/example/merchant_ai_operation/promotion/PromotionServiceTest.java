package org.example.merchant_ai_operation.promotion;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.promotion.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.example.merchant_ai_operation.promotion.dto.CreatePromotionRequest;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PromotionServiceTest {
    private static final Long TEST_SKU_ID = 1784970220075L;
    private static final Long TEST_TENANT_ID = 1001L;
    private static final Long TEST_MERCHANT_ID = 2L;
    private static final String TEST_ACTIVITY_NAME = "自动化促销测试";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PromotionService promotionService;

    @AfterEach
    void cleanUp() {
        clearTestData();
        SecurityContextHolder.clearContext();
    }

    @Test
    void merchantCanCreatePromotionAndAllocateStock() {
        clearTestData();
        mockMerchantLogin();

        CreatePromotionRequest request = new CreatePromotionRequest(
                TEST_ACTIVITY_NAME,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                TEST_SKU_ID,
                new BigDecimal("99.00"),
                1,
                1
        );

        Integer stockBefore = jdbcTemplate.queryForObject("""
                        SELECT available_stock
                        FROM product_sku
                        WHERE id = ?
                          AND tenant_id = ?
                        """,
                Integer.class,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );

        Long activityId = promotionService.createPromotionActivity(request);

        assertNotNull(activityId);

        String activityStatus = jdbcTemplate.queryForObject("""
                        SELECT status
                        FROM promotion_activities
                        WHERE id = ?
                        """,
                String.class,
                activityId
        );

        assertEquals("SCHEDULED", activityStatus);

        Integer promotionItemCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM promotion_items
                        WHERE activity_id = ?
                          AND sku_id = ?
                          AND stock_total = 1
                          AND stock_available = 1
                        """,
                Integer.class,
                activityId,
                TEST_SKU_ID
        );

        assertEquals(1, promotionItemCount);

        Integer stockAfter = jdbcTemplate.queryForObject("""
                        SELECT available_stock
                        FROM product_sku
                        WHERE id = ?
                          AND tenant_id = ?
                        """,
                Integer.class,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );

        assertEquals(stockBefore - 1, stockAfter);

        Integer movementCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM inventory_movement
                        WHERE business_type = 'PROMOTION_ALLOCATE'
                          AND business_no = ?
                          AND sku_id = ?
                          AND available_change = -1
                          AND locked_change = 0
                        """,
                Integer.class,
                "PROMOTION-" + activityId,
                TEST_SKU_ID
        );

        assertEquals(1, movementCount);
    }

    @Test
    void merchantCannotCreateOverlappingPromotionOrAllocateStock() {
        clearTestData();
        mockMerchantLogin();

        Integer stockBefore = jdbcTemplate.queryForObject("""
        SELECT available_stock
        FROM product_sku
        WHERE id = ?
          AND tenant_id = ?
        """,
                Integer.class,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );

        LocalDateTime startAt = LocalDateTime.now().plusDays(2);
        LocalDateTime endAt = startAt.plusHours(2);

        CreatePromotionRequest firstRequest = new CreatePromotionRequest(
                TEST_ACTIVITY_NAME,
                startAt,
                endAt,
                TEST_SKU_ID,
                new BigDecimal("99.00"),
                1,
                1
        );

        promotionService.createPromotionActivity(firstRequest);

        CreatePromotionRequest overlappingRequest = new CreatePromotionRequest(
                TEST_ACTIVITY_NAME,
                startAt.plusHours(1),
                endAt.plusHours(1),
                TEST_SKU_ID,
                new BigDecimal("99.00"),
                1,
                1
        );

        BizException exception = assertThrows(
                BizException.class,
                () -> promotionService.createPromotionActivity(overlappingRequest)
        );

        assertEquals(409, exception.getCode());
        assertEquals("该 SKU 在活动时间内已有促销活动", exception.getMessage());

        Integer stockAfter = jdbcTemplate.queryForObject("""
        SELECT available_stock
        FROM product_sku
        WHERE id = ?
          AND tenant_id = ?
        """,
                Integer.class,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );

        assertEquals(stockBefore - 1, stockAfter);
    }

    @Test
    void merchantCanCancelScheduledPromotionAndRestoreStock() {
        clearTestData();
        mockMerchantLogin();

        Integer stockBefore = jdbcTemplate.queryForObject("""
                        SELECT available_stock
                        FROM product_sku
                        WHERE id = ?
                          AND tenant_id = ?
                        """,
                Integer.class,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );

        CreatePromotionRequest request = new CreatePromotionRequest(
                TEST_ACTIVITY_NAME,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(2),
                TEST_SKU_ID,
                new BigDecimal("99.00"),
                1,
                1
        );

        Long activityId = promotionService.createPromotionActivity(request);
        promotionService.cancelPromotionActivity(activityId);

        String activityStatus = jdbcTemplate.queryForObject("""
                        SELECT status
                        FROM promotion_activities
                        WHERE id = ?
                        """,
                String.class,
                activityId
        );
        assertEquals("CANCELLED", activityStatus);

        Integer activityAvailableStock = jdbcTemplate.queryForObject("""
                        SELECT stock_available
                        FROM promotion_items
                        WHERE activity_id = ?
                        """,
                Integer.class,
                activityId
        );
        assertEquals(0, activityAvailableStock);

        Integer stockAfter = jdbcTemplate.queryForObject("""
                        SELECT available_stock
                        FROM product_sku
                        WHERE id = ?
                          AND tenant_id = ?
                        """,
                Integer.class,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );
        assertEquals(stockBefore, stockAfter);

        Integer releaseMovementCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM inventory_movement
                        WHERE business_type = 'PROMOTION_RELEASE'
                          AND business_no = ?
                          AND sku_id = ?
                          AND available_change = 1
                          AND locked_change = 0
                          AND available_after = ?
                          AND locked_after = 0
                        """,
                Integer.class,
                "PROMOTION-" + activityId,
                TEST_SKU_ID,
                stockBefore
        );
        assertEquals(1, releaseMovementCount);
    }

    @Test
    void merchantCannotCancelPromotionAfterItsStartTime() {
        clearTestData();
        mockMerchantLogin();

        Integer stockBefore = jdbcTemplate.queryForObject("""
                        SELECT available_stock
                        FROM product_sku
                        WHERE id = ?
                          AND tenant_id = ?
                        """,
                Integer.class,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );

        CreatePromotionRequest request = new CreatePromotionRequest(
                TEST_ACTIVITY_NAME,
                LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(4).plusHours(2),
                TEST_SKU_ID,
                new BigDecimal("99.00"),
                1,
                1
        );

        Long activityId = promotionService.createPromotionActivity(request);

        jdbcTemplate.update("""
                        UPDATE promotion_activities
                        SET start_at = DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 MINUTE)
                        WHERE id = ?
                        """,
                activityId
        );

        BizException exception = assertThrows(
                BizException.class,
                () -> promotionService.cancelPromotionActivity(activityId)
        );

        assertEquals(409, exception.getCode());
        assertEquals("只有未开始的已排期活动可以取消", exception.getMessage());

        Integer stockAfter = jdbcTemplate.queryForObject("""
                        SELECT available_stock
                        FROM product_sku
                        WHERE id = ?
                          AND tenant_id = ?
                        """,
                Integer.class,
                TEST_SKU_ID,
                TEST_TENANT_ID
        );
        assertEquals(stockBefore - 1, stockAfter);
    }

    private void clearTestData() {
        Integer allocatedStock = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(SUM(pi.stock_available), 0)
                        FROM promotion_items pi
                        JOIN promotion_activities pa ON pa.id = pi.activity_id
                        WHERE pa.tenant_id = ?
                          AND pa.name = ?
                        """,
                Integer.class,
                TEST_TENANT_ID,
                TEST_ACTIVITY_NAME
        );

        jdbcTemplate.update("""
                        DELETE FROM inventory_movement
                        WHERE business_type IN ('PROMOTION_ALLOCATE', 'PROMOTION_RELEASE')
                          AND business_no IN (
                              SELECT CONCAT('PROMOTION-', id)
                              FROM promotion_activities
                              WHERE tenant_id = ?
                                AND name = ?
                          )
                        """,
                TEST_TENANT_ID,
                TEST_ACTIVITY_NAME
        );

        jdbcTemplate.update("""
                        DELETE FROM promotion_items
                        WHERE activity_id IN (
                            SELECT id
                            FROM promotion_activities
                            WHERE tenant_id = ?
                              AND name = ?
                        )
                        """,
                TEST_TENANT_ID,
                TEST_ACTIVITY_NAME
        );

        jdbcTemplate.update("""
                        DELETE FROM promotion_activities
                        WHERE tenant_id = ?
                          AND name = ?
                        """,
                TEST_TENANT_ID,
                TEST_ACTIVITY_NAME
        );

        if (allocatedStock != null && allocatedStock > 0) {
            jdbcTemplate.update("""
                            UPDATE product_sku
                            SET available_stock = available_stock + ?,
                                version = version + 1
                            WHERE id = ?
                              AND tenant_id = ?
                            """,
                    allocatedStock,
                    TEST_SKU_ID,
                    TEST_TENANT_ID
            );
        }
    }

    private void mockMerchantLogin() {
        LoginPrincipal principal = new LoginPrincipal(
                TEST_MERCHANT_ID,
                TEST_TENANT_ID,
                "MERCHANT_ADMIN"
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
