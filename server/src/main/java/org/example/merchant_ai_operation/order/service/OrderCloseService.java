package org.example.merchant_ai_operation.order.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.inventory.entity.InventoryMovement;
import org.example.merchant_ai_operation.inventory.mapper.InventoryMovementMapper;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.order.entity.CommerceOrder;
import org.example.merchant_ai_operation.order.entity.CommerceOrderItem;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderItemMapper;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCloseService {

    private final CommerceOrderMapper commerceOrderMapper;
    private final CommerceOrderItemMapper commerceOrderItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final InventoryMovementMapper inventoryMovementMapper;

    public OrderCloseService(
            CommerceOrderMapper commerceOrderMapper,
            CommerceOrderItemMapper commerceOrderItemMapper,
            ProductSkuMapper productSkuMapper,
            InventoryMovementMapper inventoryMovementMapper
    ) {
        this.commerceOrderMapper = commerceOrderMapper;
        this.commerceOrderItemMapper = commerceOrderItemMapper;
        this.productSkuMapper = productSkuMapper;
        this.inventoryMovementMapper = inventoryMovementMapper;
    }

    //关闭过期订单事务
    @Transactional
    public void closeExpiredOrder(Long orderId){

        CommerceOrder order = commerceOrderMapper.selectById(orderId);
        if(order == null){return;}

        int closed = commerceOrderMapper.markClosedIfPendingAndExpired(
                orderId,
                LocalDateTime.now()
        );
        if (closed != 1) {return;}

        List<CommerceOrderItem> items = commerceOrderItemMapper.selectByOrderId(orderId);
        if (items.isEmpty()) {
            throw new BizException("订单明细不存在");
        }

        for (CommerceOrderItem item : items) {
            int released = productSkuMapper.releaseLockedStock(item.getSkuId(), order.getTenantId(), item.getQuantity());

            if (released != 1){
                throw new BizException("订单锁定库存释放失败");
            }

            ProductSku latestSku = productSkuMapper.selectByIdAndTenantId(
                    item.getSkuId(),
                    order.getTenantId()
            );

/*            if (latestSku == null) {
                throw new BizException("商品不存在");
            }*/
            //下方方法
            recordOrderCloseMovement(item, order, latestSku);
        }
    }

    //记录到库存账本
    private void recordOrderCloseMovement(CommerceOrderItem item, CommerceOrder order, ProductSku latestSku) {
        InventoryMovement movement = new InventoryMovement();
        movement.setTenantId(order.getTenantId());
        movement.setSkuId(item.getSkuId());
        movement.setBusinessType("ORDER_CLOSE");
        movement.setBusinessNo(order.getOrderNo());
        movement.setAvailableChange(item.getQuantity());
        movement.setLockedChange(-item.getQuantity());
        movement.setAvailableAfter(latestSku.getAvailableStock());
        movement.setLockedAfter(latestSku.getLockedStock());

        inventoryMovementMapper.insert(movement);
    }
}