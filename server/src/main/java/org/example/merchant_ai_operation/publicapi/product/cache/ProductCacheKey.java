package org.example.merchant_ai_operation.publicapi.product.cache;




public final class ProductCacheKey {

    //项目名称加缓存版本，未来 Key 结构变化时可以升级为 v2
    private static final String PREFIX = "mall:v1";
    public static final String EMPTY_DETAIL = "__EMPTY_PRODUCT_DETAIL__";

    //不用@NoArgsConstructor是因为会造成公开构造方法;
    private ProductCacheKey() {
    }

    //detail：表示这是商品详情缓存，不和商品列表、库存缓存混用。
    public static String detail(Long storeId, Long spuId) {

        return PREFIX
                + ":tenant:" + storeId//明确表示 storeId 当前对应数据库中的 tenant_id。
                + ":product:" + spuId;
    }
}
