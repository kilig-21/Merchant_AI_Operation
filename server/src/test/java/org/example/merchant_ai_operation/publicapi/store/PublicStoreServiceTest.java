package org.example.merchant_ai_operation.publicapi.store;

import org.example.merchant_ai_operation.publicapi.store.mapper.PublicStoreMapper;
import org.example.merchant_ai_operation.publicapi.store.service.PublicStoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PublicStoreServiceTest {

    @Mock
    private PublicStoreMapper publicStoreMapper;

    @InjectMocks
    private PublicStoreService publicStoreService;

    @Test
    void trimsKeywordAndUsesDefaultPagination(){
        when(publicStoreMapper.searchPublicProducts("耳机", null, 10, 0))
                .thenReturn(List.of());

        assertTrue(
                publicStoreService
                        .searchPublicProducts(" 耳机 ", null, null, null)
                        .isEmpty()
        );

        verify(publicStoreMapper)
                .searchPublicProducts("耳机", null, 10, 0);
    }

    @Test
    void capsPageSizeAtFiftyAndCalculatesOffset() {
        when(publicStoreMapper.searchPublicProducts("耳机", 1001L, 50, 100))
                .thenReturn(List.of());

        publicStoreService.searchPublicProducts(
                "耳机",
                1001L,
                3,
                999
        );

        verify(publicStoreMapper)
                .searchPublicProducts("耳机", 1001L, 50, 100);
    }

}
