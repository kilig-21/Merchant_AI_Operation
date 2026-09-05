package org.example.merchant_ai_operation.publicapi.product.service;


import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.publicapi.product.cache.ProductCacheKey;
import org.example.merchant_ai_operation.publicapi.product.vo.*;
import org.example.merchant_ai_operation.publicapi.product.mapper.PublicProductMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class PublicProductService {

    private final PublicProductMapper publicProductMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public PublicProductService(
            final PublicProductMapper publicProductMapper,
            final StringRedisTemplate stringRedisTemplate,
            final ObjectMapper objectMapper
    ) {
        this.publicProductMapper = publicProductMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }


    //获取详细商品信息
    public PublicProductDetailVO getProductDetail(Long storeId,Long spuId) {
        String cacheKey = ProductCacheKey.detail(storeId, spuId);


        String cachedJson = null;
        //因为有可能Redis 不可用:docker有时出问题,没启动
        try {
            cachedJson = stringRedisTemplate
                    .opsForValue()//表示使用 Redis 最普通的字符串类型
                    .get(cacheKey);//拿Key去查读到 JSON 字符串：缓存命中; 未读到返回 null：缓存未命中
        } catch (RuntimeException e) {
            log.warn(
                    "Redis 读取失败，回源数据库，key={}",
                    cacheKey,
                    e
                    //{} 是日志占位符；
                    //cacheKey 会填入第一个 {}；
                    //e 会把完整异常堆栈记录下来；
                    //warn 表示缓存故障，但主业务仍可继续
            );

        }

        //检测是否为空置标记
        if (ProductCacheKey.EMPTY_DETAIL.equals(cachedJson)) {
            throw new BizException(404, "商品不存在");
        }

        //Redis 有值
        //  ├─ JSON 正确：直接返回，不查数据库
        //  └─ JSON 损坏：忽略缓存，继续查数据库
        //Redis 没值：继续查数据库
        if (cachedJson != null && !cachedJson.isBlank()) {
            try {
                //如果 JSON 格式正确，就返回对象；如果格式错误，就抛出 JacksonException，进入数据库查询。
                return objectMapper.readValue( //把 JSON 字符串转回 Java 对象
                        cachedJson,
                        PublicProductDetailVO.class
                );
            } catch (JacksonException e) {
                log.warn(
                        "商品详情缓存格式错误，回源数据库，key={}",
                        cacheKey,
                        e
                );
            }
        }


        PublicProductBaseVO product = publicProductMapper.selectPublicProductDetail(storeId,spuId);
        //商品不存在两种情况:
            //这个 SPU ID 真不存在。
            //这个商品存在，但不是 ON_SALE，消费者不应该知道它存在。
        if (product == null) {
            try {
                stringRedisTemplate.opsForValue().set(
                        cacheKey,
                        ProductCacheKey.EMPTY_DETAIL,
                        java.time.Duration.ofSeconds(30)//商品不存在的过期时间为30秒
                );
            } catch (RuntimeException e) {
                log.warn(
                        "商品不存在标记写入 Redis 失败，仍返回 404，key={}",
                        cacheKey,
                        e
                );

            }
            throw new BizException(404, "商品不存在");
        }

        List<PublicSkuVO> skus=publicProductMapper.selectPublicSkusBySpuId(storeId,spuId);

        PublicProductDetailVO result = getPublicProductDetailVO(product,skus);

        //Redis写入
        //但有时写入会出问题所以得按照上面一样包一层
        String json;

        try {
            json = objectMapper.writeValueAsString(result);
        } catch (JacksonException e) {
            log.warn(
                    "商品详情序列化失败，不写入缓存，key={}",
                    cacheKey,
                    e
            );
            //序列化失败 例如:对象中有 Jackson 不支持的类型等;
            return result;
        }

        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    json,
                    java.time.Duration.ofMinutes(10)
            );
        } catch (RuntimeException e) {
            log.warn(
                    "Redis 写入失败，但商品查询仍然成功，key={}",
                    cacheKey,
                    e
            );
            //写入失败,放弃缓存,返回结果
            return result;
        }
        return result;
    }

    //查询Sku的库存
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

    //传商家的id和页数和一页的大小
    public List<PublicProductListItemVO> listStoreProducts(Long storeId, Integer page, Integer size){
        int safePage = ((page == null||page < 1) ? 1:page);
        int safeSize = size == null || size < 1 ? 10 : Math.min(size,50);
        int offset = (safePage - 1) * safeSize;

        return publicProductMapper.selectPublicProducts(storeId, safeSize, offset);
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
