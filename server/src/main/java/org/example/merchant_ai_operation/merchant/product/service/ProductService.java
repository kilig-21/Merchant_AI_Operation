package org.example.merchant_ai_operation.merchant.product.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.product.dto.CreateProductRequest;
import org.example.merchant_ai_operation.merchant.product.dto.CreateSkuRequest;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSpu;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSpuMapper;
import org.example.merchant_ai_operation.merchant.product.vo.MerchantProductVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {


    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;

    public ProductService(ProductSpuMapper productSpuMapper, ProductSkuMapper productSkuMapper) {
        this.productSpuMapper = productSpuMapper;
        this.productSkuMapper = productSkuMapper;

    }


    //创建商品类型SPU
    public Long createProductSpu(CreateProductRequest request) {

        //它从当前 JWT 登录身份里拿商家租户 ID。
        // 也就是说，就算前端偷偷传了 tenantId=1002，这里也完全不用它。
        Long tenantId = CurrentUser.requiredMerchantTenantId();

        //创建用户对象;
        ProductSpu spu = new ProductSpu();

        //在只是临时 ID 方案，和昨天注册用户 ID 一样。
        // 后面我们可以换成雪花 ID 或数据库自增，但今天先把闭环跑通。
        spu.setId(System.currentTimeMillis());
        spu.setTenantId(tenantId);
        spu.setName(request.name());
        spu.setDescription(request.description());

        //新商品先是草稿。为什么不默认上架？
        // 因为真实电商里商品通常要先补 SKU、库存、图片、价格，再上架。不然消费者可能看到半成品商品。
        spu.setStatus("DRAFT");//状态 : DRAFT


        int inserted = productSpuMapper.insert(spu);
        if (inserted != 1) {
            throw new BizException(500, "创建商品失败");
        }
        return spu.getId();
    }


    //创建具体商品SKU
    public Long createSku(Long spuId, CreateSkuRequest request){
        //首先先拿到商家id;
        Long tenantId = CurrentUser.requiredMerchantTenantId();

        //去 product_spu 表里查一下：
        //有没有一条商品，id = spuId，并且 tenant_id = 当前登录商家的 tenantId
        //说明这个商品存在，而且属于当前商家，可以继续给它新增 SKU。
        //例:如果商家 A 拿着商家 B 的 spuId 来新增 SKU，这里查不到，就返回“商品不存在”。
        // 这比直接说“你在访问别人商品”更安全，也更常见。
        int spuCount = productSpuMapper.countByIdAndTenantId(spuId, tenantId);
        if (spuCount != 1) {
            throw new BizException(404, "商品不存在");
        }


        //通过了就直接赋值初始值
        ProductSku sku = new ProductSku();
        sku.setId(System.currentTimeMillis());
        sku.setTenantId(tenantId);
        sku.setSpuId(spuId);
        sku.setSkuName(request.skuName());
        sku.setSalePrice(request.salePrice());
        sku.setAvailableStock(request.availableStock());
        sku.setLockedStock(0);
        sku.setVersion(0);
        sku.setStatus("ON_SALE");

        //再判断是否插入成功
        int inserted = productSkuMapper.insert(sku);
        if (inserted != 1) {
            throw new BizException(500, "创建SKU失败");
        }
        return  sku.getId();
    }


    //列出所有的商品的列表;
    public List<MerchantProductVO>  listMerchantProducts(Integer page,Integer size,String keyword){
        Long tenantId=CurrentUser.requiredMerchantTenantId();

        int safePage = page == null || page < 1 ? 1 : page;//前端不传 page，默认第 1 页。

        //前端不传 size，默认 10 条。
        //前端传特别大的 size=999999，后端最多给 50 条，避免一次查太多。
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, 50);

        //算偏移量:
        int offset = (safePage - 1) * safeSize;
        //把商家id,,一页显示多少个,跳过第几个传过去,传到mapper那里去;
        return productSpuMapper.selectMerchantProducts(tenantId, keyword, safeSize, offset);

    }

    //上架商品:
    //链路:
        //拿当前商家 tenantId
        //-> 确认这个商品属于当前商家
        //-> 确认商品至少有 1 个 SKU
        //-> 把 SPU 状态改成 ON_SALE
    public void publishProduct(Long spuId){
        Long  tenantId = CurrentUser.requiredMerchantTenantId();
        int spuCount=productSpuMapper.countByIdAndTenantId(spuId, tenantId);
        if (spuCount != 1) {
            throw new BizException(404,"商品不存在");
        }
        int skuCount =productSkuMapper.countBySpuIdAndTenantId(spuId, tenantId);
        if (skuCount < 1) {
            throw new BizException(409, "商品至少需要一个SKU才能上架");
        }

        // 符合条件后，把 SPU 状态改成 ON_SALE。
        int updated = productSpuMapper.updateStatusByIdAndTenantId(spuId, tenantId, "ON_SALE");
        if (updated != 1) {
            throw new BizException(500, "商品上架失败");
        }

    }


    //下架商品:
    public void unpublishProduct(Long spuId){
        Long  tenantId = CurrentUser.requiredMerchantTenantId();


        int updated = productSpuMapper.updateStatusByIdAndTenantId(spuId, tenantId, "OFF_SALE");
        //这里用 != 1，是因为按我们的业务，一个商品 ID 在当前商家下最多只能对应一条 SPU。
        // 正常成功只能是 1。0 是失败，大于 1 理论上也不正常。
        if (updated != 1) {
            throw new BizException(404, "商品不存在");
        }


    }



}

