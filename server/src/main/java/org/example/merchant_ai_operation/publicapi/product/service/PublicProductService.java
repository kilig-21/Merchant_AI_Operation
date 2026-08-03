package org.example.merchant_ai_operation.publicapi.product.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.publicapi.product.vo.*;
import org.example.merchant_ai_operation.publicapi.product.mapper.PublicProductMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicProductService {

    private final PublicProductMapper publicProductMapper;
    public PublicProductService(final PublicProductMapper publicProductMapper) {
        this.publicProductMapper = publicProductMapper;
    }

    //传商家的id和页数和一页的大小
    public List<PublicProductListItemVO> listStoreProducts(Long storeId, Integer page, Integer size){
        int safePage = ((page == null||page < 1) ? 1:page);
        int safeSize = size == null || size < 1 ? 10 : Math.min(size,50);
        int offset = (safePage - 1) * safeSize;

        return publicProductMapper.selectPublicProducts(storeId, safeSize, offset);
    }

    //获取详细商品信息
    public PublicProductDetailVO getProductDetail(Long spuId) {
        PublicProductBaseVO product = publicProductMapper.selectPublicProductDetail(spuId);
        if (product == null) {
            throw new BizException(404, "商品不存在");
            //商品不存在两种情况:
                //这个 SPU ID 真不存在。
                //这个商品存在，但不是 ON_SALE，消费者不应该知道它存在。
        }

        List<PublicSkuVO> skus=publicProductMapper.selectPublicSkusBySpuId(spuId);
        //调用下方的方法
        return getPublicProductDetailVO(product, skus);
    }

    //
    public PublicSkuAvailabilityVO getSkuAvailability(Long skuId) {
        PublicSkuAvailabilityVO availability = publicProductMapper.selectSkuAvailability(skuId);
        if (availability == null) {
            return new PublicSkuAvailabilityVO(
                    skuId,
                    false,
                    0,
                    "商品不存在或已下架"
            );
        }

        return availability;
    }



    //获取商品的详细信息方法
    private static @NonNull PublicProductDetailVO getPublicProductDetailVO(PublicProductBaseVO product, List<PublicSkuVO> skus) {
        return new PublicProductDetailVO(
                product.id(),
                product.name(),
                product.description(),
                product.updatedAt(),
                skus
        );
    }

}
