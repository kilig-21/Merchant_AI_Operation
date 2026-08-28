package org.example.merchant_ai_operation.promotion.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.inventory.entity.InventoryMovement;
import org.example.merchant_ai_operation.inventory.mapper.InventoryMovementMapper;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.promotion.dto.CreatePromotionRequest;
import org.example.merchant_ai_operation.promotion.entity.PromotionActivity;
import org.example.merchant_ai_operation.promotion.entity.PromotionItem;
import org.example.merchant_ai_operation.promotion.mapper.PromotionActivityMapper;
import org.example.merchant_ai_operation.promotion.mapper.PromotionItemMapper;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {
    private final PromotionActivityMapper promotionActivityMapper;      //创建活动
    private final PromotionItemMapper promotionItemMapper;              //查询SKU,保存活动商品
    private final ProductSkuMapper productSkuMapper;                    //从普通库存里划出
    private final InventoryMovementMapper inventoryMovementMapper;      //写入流水

    public PromotionService(
            PromotionActivityMapper promotionActivityMapper,
            PromotionItemMapper promotionItemMapper,
            ProductSkuMapper productSkuMapper,
            InventoryMovementMapper inventoryMovementMapper
    ) {
        this.promotionActivityMapper = promotionActivityMapper;
        this.promotionItemMapper = promotionItemMapper;
        this.productSkuMapper = productSkuMapper;
        this.inventoryMovementMapper = inventoryMovementMapper;
    }


    //创建活动
    @Transactional
    public Long createPromotionActivity(CreatePromotionRequest request) {
        Long tenantId = CurrentUser.requiredMerchantTenantId();

        if (!request.startAt().isBefore(request.endAt())) {
            throw new BizException(400, "活动开始时间必须早于结束时间");
        }

        //查询普通库存
        ProductSku sku = promotionItemMapper.selectSkuForPromotion(request.skuId(), tenantId);
        conditionJudge(request, sku);

        //活动冲突检查
        int overlappingCount = promotionActivityMapper.countOverlappingActivities(
                tenantId,
                request.skuId(),
                request.startAt(),
                request.endAt()
        );

        if (overlappingCount > 0) {
            throw new BizException(409, "该 SKU 在活动时间内已有促销活动");
        }

        //创建活动
        PromotionActivity activity = createActivity(request, tenantId);

        //分配库存呢
        allocateStock(request, tenantId);

        //获得最新库存消息,然后好写入流水
        ProductSku latestSku = getLatestSku(request, tenantId);

        //把流水写进数据库内
        int movementInserted = getMovementInserted(request, tenantId, activity, latestSku);

        //创建参入促销的商品
        createPromotionSku(request, activity, tenantId);

        //返回活动ID
        return activity.getId();

    }

    //取消活动并归还库存
    @Transactional
    public void cancelPromotionActivity(Long activityId) {
        Long tenantId = CurrentUser.requiredMerchantTenantId();

        PromotionItem item = cancelActivityAndRestoreStock(activityId, tenantId);

        ProductSku latestSku = promotionItemMapper.selectSkuForPromotion(
                item.getSkuId(),
                tenantId
        );
        if (latestSku == null) {
            throw new BizException(500, "归还库存后商品不存在");
        }

        recordPromotionReleaseMovement(activityId, tenantId, item, latestSku);
    }


//  <---------------- 方法提取 --------------------->


    //从数据库内最新查询,然后写进流水里
    private ProductSku getLatestSku(CreatePromotionRequest request, Long tenantId) {
        ProductSku latestSku = productSkuMapper.selectByIdAndTenantId(
                request.skuId(),
                tenantId
        );
        if (latestSku == null) {
            throw new BizException(500, "划拨库存后无法查询 SKU");
        }
        return latestSku;
    }

    //创建参入促销的商品
    private void createPromotionSku(CreatePromotionRequest request, PromotionActivity activity, Long tenantId) {
        PromotionItem item = new PromotionItem();
        item.setActivityId(activity.getId());
        item.setTenantId(tenantId);
        item.setSkuId(request.skuId());
        item.setActivityPrice(request.activityPrice());
        item.setStockTotal(request.stockTotal());
        item.setStockAvailable(request.stockTotal());
        item.setLimitPerUser(request.limitPerUser());

        int itemInserted = promotionItemMapper.insert(item);

        if (itemInserted != 1) {
            throw new BizException(500, "保存促销商品失败");
        }
    }

    //创建活动写入流水
    private int getMovementInserted(CreatePromotionRequest request, Long tenantId, PromotionActivity activity, ProductSku latestSku) {
        InventoryMovement movement = new InventoryMovement();
        movement.setTenantId(tenantId);
        movement.setSkuId(request.skuId());
        movement.setBusinessType("PROMOTION_ALLOCATE");
        movement.setBusinessNo("PROMOTION-" + activity.getId());
        movement.setAvailableChange(-request.stockTotal());
        movement.setLockedChange(0);
        movement.setAvailableAfter(latestSku.getAvailableStock());
        movement.setLockedAfter(latestSku.getLockedStock());

        int movementInserted = inventoryMovementMapper.insert(movement);
        if (movementInserted != 1) {
            throw new BizException(500, "写入促销库存流水失败");
        }
        return movementInserted;
    }

    //活动取消库存释放流水
    private void recordPromotionReleaseMovement(Long activityId, Long tenantId, PromotionItem item, ProductSku latestSku) {
        InventoryMovement movement = new InventoryMovement();
        movement.setTenantId(tenantId);
        movement.setSkuId(item.getSkuId());
        movement.setBusinessType("PROMOTION_RELEASE");
        movement.setBusinessNo("PROMOTION-" + activityId);
        movement.setAvailableChange(item.getStockAvailable());
        movement.setLockedChange(0);
        movement.setAvailableAfter(latestSku.getAvailableStock());
        movement.setLockedAfter(latestSku.getLockedStock());

        if (inventoryMovementMapper.insert(movement) != 1) {
            throw new BizException(500, "记录促销库存归还流水失败");
        }
    }

    //分配库存
    private void allocateStock(CreatePromotionRequest request, Long tenantId) {
        int allocated = productSkuMapper.allocatePromotionStock(
                request.skuId(),
                tenantId,
                request.stockTotal()
        );

        if (allocated != 1) {
            throw new BizException(409, "活动库存划拨失败，普通库存可能已变化");
        }
    }

    //创建活动
    private PromotionActivity createActivity(CreatePromotionRequest request, Long tenantId) {
        PromotionActivity activity = new PromotionActivity();
        activity.setTenantId(tenantId);
        activity.setName(request.name());
        activity.setStartAt(request.startAt());
        activity.setEndAt(request.endAt());
        activity.setStatus("SCHEDULED");

        int activityInserted = promotionActivityMapper.insert(activity);

        if (activityInserted != 1) {
            throw new BizException(500, "创建促销活动失败");
        }
        return activity;
    }

    //活动取消释放活动库存
    private PromotionItem cancelActivityAndRestoreStock(Long activityId, Long tenantId) {
        PromotionItem item = promotionItemMapper.selectByActivityId(activityId, tenantId);
        if (item == null) {
            throw new BizException(404, "促销活动不存在");
        }

        int cancelled = promotionActivityMapper.cancelScheduledActivity(activityId, tenantId);
        if (cancelled != 1) {
            throw new BizException(409, "只有未开始的已排期活动可以取消");
        }

        int cleared = promotionItemMapper.clearAvailableStock(activityId, tenantId);
        if (cleared != 1) {
            throw new BizException(409, "活动库存状态异常，无法取消");
        }

        int restored = productSkuMapper.restorePromotionStock(
                item.getSkuId(),
                tenantId,
                item.getStockAvailable()
        );
        if (restored != 1) {
            throw new BizException(500, "归还促销库存失败");
        }

        return item;
    }

    //查询出sku是否有不符合的
    private static void conditionJudge(CreatePromotionRequest request, ProductSku sku) {
        if (sku == null) {
            throw new BizException(404, "SKU不存在");
        }

        if (!"ON_SALE".equals(sku.getStatus())) {
            throw new BizException(400, "SKU未上架，不能参加促销");
        }

        if (request.activityPrice().compareTo(sku.getSalePrice()) > 0) {
            throw new BizException(400, "活动价格不能高于普通销售价格");
        }

        if (request.stockTotal() > sku.getAvailableStock()) {
            throw new BizException(400, "活动库存不能超过当前可售库存");
        }
    }

}
