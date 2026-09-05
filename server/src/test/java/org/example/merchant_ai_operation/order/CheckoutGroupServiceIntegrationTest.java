package org.example.merchant_ai_operation.order;

import org.example.merchant_ai_operation.order.entity.CheckoutGroup;
import org.example.merchant_ai_operation.order.service.CheckoutGroupService;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CheckoutGroupServiceIntegrationTest {

    @Autowired
    private CheckoutGroupService checkoutGroupService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testGroupId;

    @BeforeEach
    void login() {
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
    void cleanUp() {
        if (testGroupId != null) {
            jdbcTemplate.update(
                    "DELETE FROM checkout_group WHERE id = ?",
                    testGroupId
            );
        }

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateAndReadCheckoutGroupFromDatabase() {
        CheckoutGroup created = checkoutGroupService.createPending(new BigDecimal("399.00"));

        testGroupId = created.getId();

        assertNotNull(testGroupId);
        assertEquals(5001L, created.getConsumerId());
        assertEquals("PENDING_PAYMENT", created.getStatus());
        assertEquals(
                new BigDecimal("399.00"),
                created.getTotalAmount()
        );

        CheckoutGroup loaded =
                checkoutGroupService.getMine(testGroupId);

        assertEquals(testGroupId, loaded.getId());
        assertEquals(created.getCheckoutNo(), loaded.getCheckoutNo());
    }


}
