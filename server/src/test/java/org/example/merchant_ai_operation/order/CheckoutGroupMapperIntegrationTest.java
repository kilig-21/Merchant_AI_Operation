package org.example.merchant_ai_operation.order;

import org.example.merchant_ai_operation.order.entity.CheckoutGroup;
import org.example.merchant_ai_operation.order.mapper.CheckoutGroupMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CheckoutGroupMapperIntegrationTest {

    @Autowired
    private CheckoutGroupMapper checkoutGroupMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String testCheckoutNo;

    @AfterEach
    void cleanUp() {
        if (testCheckoutNo != null) {
            jdbcTemplate.update(
                    "DELETE FROM checkout_group WHERE checkout_no = ?",
                    testCheckoutNo
            );
        }
    }

    @Test
    void shouldInsertAndReadCheckoutGroupWithConsumerIsolation() {
        testCheckoutNo = "TEST-CHK-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24);

        CheckoutGroup group = new CheckoutGroup();
        group.setCheckoutNo(testCheckoutNo);
        group.setConsumerId(5001L);
        group.setStatus("PENDING_PAYMENT");
        group.setTotalAmount(new BigDecimal("199.00"));

        int inserted = checkoutGroupMapper.insert(group);

        assertEquals(1, inserted);
        assertNotNull(group.getId());

        CheckoutGroup mine =
                checkoutGroupMapper.selectByIdAndConsumerId(
                        group.getId(),
                        5001L
                );

        assertNotNull(mine);
        assertEquals(testCheckoutNo, mine.getCheckoutNo());
        assertEquals(new BigDecimal("199.00"), mine.getTotalAmount());

        CheckoutGroup otherConsumer = checkoutGroupMapper.selectByIdAndConsumerId(
                        group.getId(),
                        5002L
                );

        assertNull(otherConsumer);
    }
}