package org.example.merchant_ai_operation.publicapi.store.service;


import org.example.merchant_ai_operation.publicapi.store.mapper.PublicStoreMapper;
import org.example.merchant_ai_operation.publicapi.store.vo.PublicMarketplaceProductVO;
import org.example.merchant_ai_operation.publicapi.store.vo.PublicStoreSummaryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicStoreService {

    private final PublicStoreMapper  publicStoreMapper;
    public PublicStoreService(PublicStoreMapper publicStoreMapper) {
        this.publicStoreMapper = publicStoreMapper;
    }

    public List<PublicStoreSummaryVO> listPublicStores() {
        return publicStoreMapper.selectPublicStores();
    }

    //列出商品和商家的具体信息
    public List<PublicMarketplaceProductVO> searchPublicProducts(
            String keyword,
            Long storeId,
            Integer page,
            Integer size
    ){
        //先去掉首尾空格
        String safeKeyword = keyword == null ? null : keyword.trim();

        //分页
        int safePage = page == null || page < 1 ? 1:page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size,50);
        int offset = (safePage - 1) * safeSize;

        return publicStoreMapper.searchPublicProducts(
                safeKeyword,
                storeId,
                safeSize,
                offset
        );
    }


}
