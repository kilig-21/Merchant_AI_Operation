package org.example.merchant_ai_operation;

import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.order.message.OrderCloseMessage;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicProductDetailVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Contracts persisted before the Jackson 3 migration must remain readable. */
@SpringBootTest
class JsonCompatibilityTest {
    @Autowired
    ObjectMapper mapper;

    @Test
    void shouldPreserveApiResponseShapeAndDecimalAmount() {
        var json = mapper.readTree(mapper.writeValueAsString(ApiResponse.ok(new BigDecimal("199.20"))));
        assertEquals(0, json.get("code").asInt());
        assertEquals("ok", json.get("message").asText());
        assertEquals(0, new BigDecimal("199.20").compareTo(json.get("data").decimalValue()));
        var error = mapper.readTree(mapper.writeValueAsString(ApiResponse.error(401, "请先登录")));
        assertEquals(401, error.get("code").asInt());
        assertEquals("请先登录", error.get("message").asText());
        assertTrue(error.get("data").isNull());
    }

    @Test
    void shouldReadExistingProductCacheAndKeepIsoDateTime() {
        String cached = """
                {"id":1,"name":"商品","description":null,"updatedAt":"2026-09-04T12:30:00",
                 "skus":[{"id":2,"skuName":"标准款","salePrice":199.20,"availableStock":10}]}
                """;
        var product = mapper.readValue(cached, PublicProductDetailVO.class);
        assertEquals(new BigDecimal("199.20"), product.skus().getFirst().salePrice());
        assertEquals(LocalDateTime.of(2026, 9, 4, 12, 30), product.updatedAt());
        assertTrue(mapper.readTree(mapper.writeValueAsString(product)).get("updatedAt").isString());
    }

    @Test
    void shouldReadExistingOutboxOrderCloseMessage() {
        var event = mapper.readValue("""
                {"orderId":123,"orderNo":"ORD-123","expireAt":"2026-09-04T12:30:00"}
                """, OrderCloseMessage.class);
        assertEquals(123L, event.orderId());
        assertEquals(LocalDateTime.of(2026, 9, 4, 12, 30), event.expireAt());
        assertEquals(event, mapper.readValue(mapper.writeValueAsString(event), OrderCloseMessage.class));
    }
}
