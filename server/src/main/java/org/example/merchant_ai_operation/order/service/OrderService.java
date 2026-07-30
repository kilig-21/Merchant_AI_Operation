package org.example.merchant_ai_operation.order.service;

import org.example.merchant_ai_operation.cart.mapper.CartItemMapper;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderItemMapper;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;
import org.example.merchant_ai_operation.order.vo.CreateOrderVO;
import org.example.merchant_ai_operation.order.vo.OrderSkuSnapshotVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final ProductSkuMapper productSkuMapper;
    private final CommerceOrderMapper commerceOrderMapper;
    private final CommerceOrderItemMapper commerceOrderItemMapper;
    private final CartItemMapper cartItemMapper;

    public OrderService(ProductSkuMapper productSkuMapper,
                        CommerceOrderMapper commerceOrderMapper,
                        CommerceOrderItemMapper commerceOrderItemMapper,
                        CartItemMapper cartItemMapper
                        ) {
        this.productSkuMapper = productSkuMapper;
        this.commerceOrderMapper = commerceOrderMapper;
        this.commerceOrderItemMapper = commerceOrderItemMapper;
        this.cartItemMapper = cartItemMapper;
    }


    @Transactional
    public CreateOrderVO createOrderVO(CreateOrderRequest request) {
        Long consumerId = CurrentUser.requiredConsumerId();

        List<OrderSkuSnapshotVO> snapshots = productSkuMapper.selectOrderSkuSnapshots(
                consumerId,
                request.cartItemIds()
        );

        if (snapshots.size() != request.cartItemIds().size()) {
            throw new BizException("购物车项不存在");
        }

        Long tenantId = snapshots.getFirst().tenantId();

        for (OrderSkuSnapshotVO snapshot : snapshots) {
            if (!tenantId.equals(snapshot.tenantId())) {
                throw new BizException("暂不支持跨商家合并下单");
            }

            if (!"ON_SALE".equals(snapshot.skuStatus()) || !"ON_SALE".equals(snapshot.spuStatus())) {
                throw new BizException("商品已下架");
            }

            if (snapshot.quantity() == null || snapshot.quantity() <= 0) {
                throw new BizException("购物车商品数量不正确");
            }

            if (snapshot.availableStock() < snapshot.quantity()) {
                throw new BizException("商品库存不足");
            }
        }
        return null;
    }

}

