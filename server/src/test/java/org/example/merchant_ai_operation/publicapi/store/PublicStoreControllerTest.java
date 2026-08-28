package org.example.merchant_ai_operation.publicapi.store;


import org.example.merchant_ai_operation.publicapi.store.controller.PublicStoreController;
import org.example.merchant_ai_operation.publicapi.store.service.PublicStoreService;
import org.example.merchant_ai_operation.publicapi.store.vo.PublicMarketplaceProductVO;
import org.example.merchant_ai_operation.publicapi.store.vo.PublicStoreSummaryVO;
import org.example.merchant_ai_operation.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(PublicStoreController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PublicStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicStoreService publicStoreService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void listsStores() throws Exception {
        when(publicStoreService.listPublicStores())
                .thenReturn(List.of(
                        new PublicStoreSummaryVO(1001L, "kilig数码旗舰店", 1)
                ));

        mockMvc.perform(get("/api/public/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(1001))
                .andExpect(jsonPath("$.data[0].productCount").value(1));
    }

    @Test
    void searchesProducts() throws Exception {
        when(publicStoreService.searchPublicProducts(
                "耳机", 1001L, 1, 10
        )).thenReturn(List.of(
                new PublicMarketplaceProductVO(
                        1001L,
                        "kilig数码旗舰店",
                        1784967699881L,
                        "蓝牙耳机",
                        "第一款商家商品",
                        new java.math.BigDecimal("188.00"),
                        26,
                        java.time.LocalDateTime.parse("2026-08-06T06:52:07")
                )
        ));

        mockMvc.perform(get("/api/public/stores/products/search")
                        .param("keyword", "耳机")
                        .param("storeId", "1001")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].storeId").value(1001))
                .andExpect(jsonPath("$.data[0].name").value("蓝牙耳机"));
    }
}
