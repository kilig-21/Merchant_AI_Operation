package org.example.merchant_ai_operation.order.service;

import org.example.merchant_ai_operation.cart.mapper.CartItemMapper;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.entity.CommerceOrder;
import org.example.merchant_ai_operation.order.entity.CommerceOrderItem;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderItemMapper;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;
import org.example.merchant_ai_operation.order.vo.CreateOrderVO;
import org.example.merchant_ai_operation.order.vo.OrderSkuSnapshotVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

        //从下方的方法使用
        Long tenantId = getTenantId(snapshots);

        //算金额的数量
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderSkuSnapshotVO snapshot : snapshots) {
            totalAmount = totalAmount.add(
                    //snapshot.quantity()返回的是Integer不是BigDecimal
                    //所以要转换过去
                    snapshot.salePrice().multiply(BigDecimal.valueOf(snapshot.quantity()))
            );
        }




        //创建订单主表:
        CommerceOrder order = new CommerceOrder();
        order.setOrderNo(generateOrderNo());            //生成业务订单号
        order.setTenantId(tenantId);                    //记录这笔订单属于哪个商家
        order.setConsumerId(consumerId);                //记录是谁买的
        order.setStatus("PENDING_PAYMENT");             //订单刚创建，还没支付，所以状态是“待支付”
        order.setTotalAmount(totalAmount);              //将上面算好的金额放进去
        order.setExpireAt(LocalDateTime.now().plusMinutes(30)); //设置 30 分钟后过期。后面做超时关单时会用到。

        //将设好值的订单通过mapper放进数据库里
        commerceOrderMapper.insert(order);



        //把每个购物车项变成订单项，同时锁定库存，并把已结算的购物车项删掉。
        for (OrderSkuSnapshotVO snapshot : snapshots){
            int locked = productSkuMapper.lockStock(
                    snapshot.skuId(),
                    snapshot.tenantId(),
                    snapshot.quantity()
            );

            //判断锁库存是否成功
            if(locked != 1){
                throw new BizException("商品库存不足");
            }

            CommerceOrderItem item = new CommerceOrderItem();
            item.setOrderId(order.getId());                 //这条明细属于刚创建的订单主表。
            item.setSkuId(snapshot.skuId());                //记录买的是哪个 SKU。
            item.setSkuNameSnapshot(snapshot.skuName());    //保存商品名快照。以后商品改名了，历史订单里的名字也不变。
            item.setSalePrice(snapshot.salePrice());        //保存下单当时的真实价格。以后商品改价了，历史订单金额也不变。
            item.setQuantity(snapshot.quantity());          //保存买了几个


            //写订单明细，保存 SKU 名称和价格快照。
            commerceOrderItemMapper.insert(item);
            cartItemMapper.deleteByIdAndConsumerId(snapshot.cartItemId(), consumerId);

        }

        return new CreateOrderVO(
                order.getId(),
                order.getOrderNo(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getExpireAt()
        );
    }

    private Long getTenantId(List<OrderSkuSnapshotVO> snapshots) {
        Long tenantId = snapshots.getFirst().tenantId();

        //进行一系列的检查
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
        return tenantId;
    }

    //创建订单号码
    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "ORD" + datePart + randomPart;
    }
}

