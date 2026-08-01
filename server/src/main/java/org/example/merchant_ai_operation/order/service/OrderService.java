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
import org.example.merchant_ai_operation.order.vo.OrderDetailVO;
import org.example.merchant_ai_operation.order.vo.OrderItemVO;
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


    //创建订单的service方法
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


    @Transactional
    public void mockPay(Long orderId){
        Long consumerId = CurrentUser.requiredConsumerId();

        int paid = commerceOrderMapper.markPaidByIdAndConsumerId(orderId, consumerId);

        if(paid != 1){
            throw new BizException("订单不存在或状态不允许支付");
        }

        //创建订单明细表
        List<CommerceOrderItem> items = commerceOrderItemMapper.selectByOrderId(orderId);
        if (items.isEmpty()) {
            throw new BizException("订单明细不存在");
        }

        for (CommerceOrderItem item : items) {
            //依次遍历订单从锁定库存中移出
            int deducted = productSkuMapper.deductLockedStock(
                    item.getSkuId(),
                    item.getQuantity()
            );
            //一般一次就释放一个订单,所以这里如果不等于1就失败;通过@Transactional直接全部退回;
            if (deducted != 1) {
                throw new BizException("订单锁定库存异常");
            }
        }
    }

    //列出订单列表
    public List<OrderDetailVO> listMyOrders(){
        Long  consumerId = CurrentUser.requiredConsumerId();

        return commerceOrderMapper.selectByConsumerId(consumerId)
                .stream()
                .map(order -> new OrderDetailVO(
                        order.getId(),
                        order.getOrderNo(),
                        order.getTenantId(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getExpireAt(),
                        order.getCreatedAt(),

                        List.of()           //表示的是OrderDetailVO里的items
                        //这个是列表版。为什么 items 用 List.of()？
                        //订单列表通常只展示订单主信息，不把每笔订单的所有明细都查出来，避免以后订单多了以后列表接口很重。详情页再查明细
                ))
                .toList();
    }

    //展示某个订单的详情信息
    public OrderDetailVO getMyOrderDetail(Long orderId){
        Long consumerId = CurrentUser.requiredConsumerId();

        CommerceOrder order = commerceOrderMapper.selectByOrderIdAndConsumerId(orderId,consumerId);

        if (order == null){
            throw new BizException("订单不存在");
        }

        List<OrderItemVO> items = commerceOrderItemMapper.selectItemVOByOrderId(orderId);

        return new OrderDetailVO(
                order.getId(),
                order.getOrderNo(),
                order.getTenantId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getExpireAt(),
                order.getCreatedAt(),
                items
        );
    }

    //从快照里获得商家id
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

