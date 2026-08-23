package org.example.merchant_ai_operation.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.merchant_ai_operation.cart.mapper.CartItemMapper;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.idempotency.entity.IdempotentRequest;
import org.example.merchant_ai_operation.idempotency.mapper.IdempotentRequestMapper;
import org.example.merchant_ai_operation.inventory.entity.InventoryMovement;
import org.example.merchant_ai_operation.inventory.mapper.InventoryMovementMapper;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.entity.CommerceOrder;
import org.example.merchant_ai_operation.order.entity.CommerceOrderItem;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderItemMapper;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;
import org.example.merchant_ai_operation.order.vo.*;
import org.example.merchant_ai_operation.outbox.entity.OutboxEvent;
import org.example.merchant_ai_operation.outbox.mapper.OutboxEventMapper;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;




@Service
public class OrderService {
    private final ProductSkuMapper productSkuMapper;
    private final CommerceOrderMapper commerceOrderMapper;
    private final CommerceOrderItemMapper commerceOrderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final InventoryMovementMapper  inventoryMovementMapper;
    private final IdempotentRequestMapper idempotentRequestMapper;
    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;
    private final Clock applicationClock;
    private final CommerceOrderAddressService commerceOrderAddressService;

    public OrderService(ProductSkuMapper productSkuMapper,
                        CommerceOrderMapper commerceOrderMapper,
                        CommerceOrderItemMapper commerceOrderItemMapper,
                        CartItemMapper cartItemMapper,
                        InventoryMovementMapper inventoryMovementMapper,
                        IdempotentRequestMapper idempotentRequestMapper,
                        OutboxEventMapper outboxEventMapper,
                        ObjectMapper objectMapper,
                        Clock applicationClock,
                        CommerceOrderAddressService commerceOrderAddressService

    ) {
        this.productSkuMapper = productSkuMapper;
        this.commerceOrderMapper = commerceOrderMapper;
        this.commerceOrderItemMapper = commerceOrderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.inventoryMovementMapper = inventoryMovementMapper;
        this.idempotentRequestMapper = idempotentRequestMapper;
        this.outboxEventMapper = outboxEventMapper;
        this.objectMapper = objectMapper;
        this.applicationClock = applicationClock;
        this.commerceOrderAddressService = commerceOrderAddressService;
    }


    //原来的单一商家结算订单方法
    @Transactional
    public CreateOrderVO createOrderVO(
            String idempotencyKey,
            CreateOrderRequest request) {
        return createOrderVO(idempotencyKey, request, null);
    }

    //创建订单的service方法(新的跨商店结算订单)
    @Transactional
    public CreateOrderVO createOrderVO(String idempotencyKey,
                                       CreateOrderRequest request,
                                       Long checkoutGroupId) {
        Long consumerId = CurrentUser.requiredConsumerId();

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BizException(400, "Idempotency-Key 不能为空");
        }
        //创建请求的哈希值;
        //方法在下方
        String requestHash = buildCreateOrderRequestHash(request);

        //先验证订单是否有重复的
        IdempotentRequest idempotentRequest =
                idempotentRequestMapper.selectByConsumerIdAndRequestKey(consumerId, idempotencyKey);

        if (idempotentRequest != null) {
            if (!requestHash.equals(idempotentRequest.getRequestHash())) {
                throw new BizException("同一个 Idempotency-Key 不能用于不同下单参数");
            }

            if ("SUCCESS".equals(idempotentRequest.getStatus())) {
                CommerceOrder existingOrder = commerceOrderMapper.selectByOrderIdAndConsumerId(
                        idempotentRequest.getOrderId(),
                        consumerId
                );

                if (existingOrder == null) {
                    throw new BizException("幂等记录对应的订单不存在");
                }

                return new CreateOrderVO(
                        existingOrder.getId(),
                        existingOrder.getOrderNo(),
                        existingOrder.getStatus(),
                        existingOrder.getTotalAmount(),
                        existingOrder.getExpireAt()
                );
            }

            throw new BizException(409, "请求正在处理中，请稍后查询订单结果");
        }

        idempotentRequest = new IdempotentRequest();
        idempotentRequest.setConsumerId(consumerId);
        idempotentRequest.setRequestKey(idempotencyKey);
        idempotentRequest.setRequestHash(requestHash);
        idempotentRequest.setStatus("PROCESSING");
        /*
         * 要给 insert 包一层 try/catch，否则并发时会因为数据库唯一约束抛异常
         * */
        // 原来的 : idempotentRequestMapper.insert(idempotentRequest);
        try {
            idempotentRequestMapper.insert(idempotentRequest);
        } catch (DuplicateKeyException ex) {
            throw new BizException(409, "请求正在处理中，请稍后查询订单结果");
        }


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
        CommerceOrder order = createOrder(tenantId, consumerId, totalAmount,checkoutGroupId);
        //将设好值的订单通过mapper放进数据库里
        commerceOrderMapper.insert(order);

        if (request.addressId() != null) {
            commerceOrderAddressService.createSnapshot(
                    order.getId(),
                    request.addressId()
            );
        }


        //把每个购物车项变成订单项，同时锁定库存，并把已结算的购物车项删掉。
        for (OrderSkuSnapshotVO snapshot : snapshots) {
            int locked = productSkuMapper.lockStock(
                    snapshot.skuId(),
                    snapshot.tenantId(),
                    snapshot.quantity()
            );

            //判断锁库存是否成功
            if (locked != 1) {
                throw new BizException("商品库存不足");
            }

            //lockStock(...) 成功后，数据库里的 available_stock / locked_stock 已经变化了。
            //这时再查一次 SKU，就能拿到变化后的库存。
            //然后把“本次变化量 + 变化后余额”写入 inventory_movement。
            ProductSku latestSku = productSkuMapper.selectByIdAndTenantId(
                    snapshot.skuId(),
                    snapshot.tenantId()
            );
            if (latestSku == null) {
                throw new BizException("商品不存在");
            }

            //注意：这段代码也在 @Transactional 里面。
            //所以如果后面写订单明细失败，或者删除购物车失败，这条库存流水也会一起回滚，不会留下半截账。
            //创建变化后的流水记录
            InventoryMovement movement = createOrderLockMovement(snapshot, order, latestSku);
            inventoryMovementMapper.insert(movement);


            //创建订单详情:
            CommerceOrderItem item = createOrderItem(snapshot, order);
            //写订单明细，保存 SKU 名称和价格快照。
            commerceOrderItemMapper.insert(item);
            //创建好后就把购物车里对应的商品出去了
            cartItemMapper.deleteByIdAndConsumerId(snapshot.cartItemId(), consumerId);

        }

        //写入RabbitMQ消息
        OutboxEvent event = createOrderCreatedEvent(order);
        outboxEventMapper.insert(event);

        //订单好了后再return之前把订单成功的记录传过去;->把订单id给写回idempotent_request.order_id
        //新增一个前提,如果id为null则按旧的方法参数走,有值则为走新的三参数
        if (checkoutGroupId == null) {
            idempotentRequestMapper.markSuccess(
                    idempotentRequest.getId(),
                    order.getId()
            );
        } else {
            idempotentRequestMapper.markSuccessWithCheckoutGroup(
                    idempotentRequest.getId(),
                    order.getId(),
                    checkoutGroupId
            );
        }

        return new CreateOrderVO(
                order.getId(),
                order.getOrderNo(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getExpireAt()
        );
    }

    //模拟支付订单
    @Transactional
    public void mockPay(Long orderId){
        Long consumerId = CurrentUser.requiredConsumerId();

        int paid = commerceOrderMapper.markPaidByIdAndConsumerId(
                orderId,
                consumerId,
                LocalDateTime.now(applicationClock)
        );

        if(paid != 1){throw new BizException("订单不存在或状态不允许支付");}

        CommerceOrder order = commerceOrderMapper.selectByOrderIdAndConsumerId(orderId, consumerId);

        if (order == null) {throw new BizException("订单不存在");}

        // 查询订单明细：支付成功后需要知道这笔订单买了哪些 SKU、每个 SKU 买了几个。
        List<CommerceOrderItem> items = commerceOrderItemMapper.selectByOrderId(orderId);

        if (items.isEmpty()) {throw new BizException("订单明细不存在");}

        for (CommerceOrderItem item : items) {
            //依次遍历订单从锁定库存中移出
            int deducted = productSkuMapper.deductLockedStock(
                    item.getSkuId(),
                    item.getQuantity()
            );
            //一般一次就释放一个订单,所以这里如果不等于1就失败;通过@Transactional直接全部退回;
            if (deducted != 1) {throw new BizException("订单锁定库存异常");}

            ProductSku latestSku = productSkuMapper.selectByIdAndTenantId(
                    item.getSkuId(),
                    order.getTenantId()
            );
            if (latestSku == null) {throw new BizException("商品不存在");}

            //创建变化后的流水记录
            InventoryMovement movement = createOrderPaidMovement(item, order, latestSku);
            inventoryMovementMapper.insert(movement);
        }
    }

    //列出订单列表
    public List<OrderDetailVO> listMyOrders(){
        Long  consumerId = CurrentUser.requiredConsumerId();

        return commerceOrderMapper.selectByConsumerId(consumerId)
                .stream()
                .map(order -> new OrderDetailVO(
                        order.getId(),
                        order.getCheckoutGroupId(),
                        order.getOrderNo(),
                        order.getTenantId(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getExpireAt(),
                        order.getCreatedAt(),

                        List.of(),          //表示的是OrderDetailVO里的items
                        //这个是列表版。为什么 items 用 List.of()？
                        //订单列表通常只展示订单主信息，不把每笔订单的所有明细都查出来，避免以后订单多了以后列表接口很重。详情页再查明细
                        null
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

        //订单明细
        List<OrderItemVO> items = commerceOrderItemMapper.selectItemVOByOrderId(orderId);

        //地址明细
        OrderAddressSnapshotVO shippingAddress = commerceOrderAddressService.getSnapshot(orderId);

        return new OrderDetailVO(
                order.getId(),
                order.getCheckoutGroupId(),
                order.getOrderNo(),
                order.getTenantId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getExpireAt(),
                order.getCreatedAt(),
                items,
                shippingAddress
        );
    }

    //取消订单
    @Transactional
    public void cancelOrder(Long  orderId){
        Long consumerId = CurrentUser.requiredConsumerId();

        //标记取消订单
        int cancelled = commerceOrderMapper.markCancelledByIdAndConsumerId(orderId, consumerId);
        //只要没有成功更新，就停止后续库存操作，避免错误释放库存。
        if (cancelled != 1){
            throw new BizException("订单不存在或状态不允许取消");
        }

        //重新查一次,获得最新的数据库的信息
        CommerceOrder order = commerceOrderMapper.selectByOrderIdAndConsumerId(orderId, consumerId);
        //查询完后列出
        List<CommerceOrderItem> items = commerceOrderItemMapper.selectByOrderId(orderId);

        for (CommerceOrderItem item : items) {
            int released =  productSkuMapper.releaseLockedStock(
                    item.getSkuId(),
                    order.getTenantId(),
                    item.getQuantity()
            );
            if(released != 1){
                throw new BizException("订单锁定库存释放失败");
            }
            //释放完后重新
            ProductSku latestSku = productSkuMapper.selectByIdAndTenantId(
                    item.getSkuId(),
                    order.getTenantId()
            );
            //写入流水
            InventoryMovement movement = createOrderCancelMovement(item, order, latestSku);
            inventoryMovementMapper.insert(movement);
        }
    }


    // ==================== 方法抽取 ==================== //

    //创建订单:
    private @NonNull CommerceOrder createOrder(Long tenantId, Long consumerId, BigDecimal totalAmount,Long checkoutGroupId) {
        CommerceOrder order = new CommerceOrder();
        order.setOrderNo(generateOrderNo());            //生成业务订单号
        order.setTenantId(tenantId);                    //记录这笔订单属于哪个商家
        order.setConsumerId(consumerId);                //记录是谁买的
        order.setStatus("PENDING_PAYMENT");             //订单刚创建，还没支付，所以状态是“待支付”
        order.setTotalAmount(totalAmount);              //将上面算好的金额放进去
        order.setExpireAt(LocalDateTime.now(applicationClock).plusMinutes(30)); //设置 30 分钟后过期。后面做超时关单时会用到。
        order.setCheckoutGroupId(checkoutGroupId);      //把group的id加进去
        return order;
    }

    //创建订单详情:
    private static @NonNull CommerceOrderItem createOrderItem(OrderSkuSnapshotVO snapshot, CommerceOrder order) {
        CommerceOrderItem item = new CommerceOrderItem();
        item.setOrderId(order.getId());                 //这条明细属于刚创建的订单主表。
        item.setSkuId(snapshot.skuId());                //记录买的是哪个 SKU。
        item.setSkuNameSnapshot(snapshot.skuName());    //保存商品名快照。以后商品改名了，历史订单里的名字也不变。
        item.setSalePrice(snapshot.salePrice());        //保存下单当时的真实价格。以后商品改价了，历史订单金额也不变。
        item.setQuantity(snapshot.quantity());          //保存买了几个
        return item;
    }

    //创建变化前的流水记录/下单锁库流水
    private static @NonNull InventoryMovement createOrderLockMovement(OrderSkuSnapshotVO snapshot, CommerceOrder order, ProductSku latestSku) {
        InventoryMovement movement = new InventoryMovement();
        movement.setTenantId(snapshot.tenantId());
        movement.setSkuId(snapshot.skuId());
        movement.setBusinessType("ORDER_LOCK");
        movement.setBusinessNo(order.getOrderNo());
        movement.setAvailableChange(-snapshot.quantity());
        movement.setLockedChange(snapshot.quantity());
        movement.setAvailableAfter(latestSku.getAvailableStock());
        movement.setLockedAfter(latestSku.getLockedStock());
        return movement;
    }

    //创建变化后的流水记录/支付流水
    private static @NonNull InventoryMovement createOrderPaidMovement(CommerceOrderItem item, CommerceOrder order, ProductSku latestSku) {
        InventoryMovement movement = new InventoryMovement();
        movement.setTenantId(order.getTenantId());
        movement.setSkuId(item.getSkuId());
        movement.setBusinessType("ORDER_PAID");
        movement.setBusinessNo(order.getOrderNo());
        movement.setAvailableChange(0);
        movement.setLockedChange(-item.getQuantity());
        movement.setAvailableAfter(latestSku.getAvailableStock());
        movement.setLockedAfter(latestSku.getLockedStock());
        return movement;
    }

    //创建取消后的流水记录
    private InventoryMovement createOrderCancelMovement(CommerceOrderItem item, CommerceOrder order, ProductSku latestSku) {
        InventoryMovement movement = new InventoryMovement();
        movement.setTenantId(order.getTenantId());
        movement.setSkuId(item.getSkuId());
        movement.setBusinessType("ORDER_CANCEL");
        movement.setBusinessNo(order.getOrderNo());
        movement.setAvailableChange(item.getQuantity());
        movement.setLockedChange(-item.getQuantity());
        movement.setAvailableAfter(latestSku.getAvailableStock());
        movement.setLockedAfter(latestSku.getLockedStock());
        return movement;
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
        String datePart = LocalDateTime.now(applicationClock).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "ORD" + datePart + randomPart;
    }

    // 创建下单请求的参数指纹：同一批购物车项会得到同一个字符串
    private String buildCreateOrderRequestHash(CreateOrderRequest request) {
        String cartItemPart = request.cartItemIds()
                .stream()
                .sorted()
                .map(String::valueOf)
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        return cartItemPart
                + "|addressId="
                + (request.addressId() == null ? "" : request.addressId());
    }

    //写入订单时间消息
    private OutboxEvent createOrderCreatedEvent(CommerceOrder order) {
        OutboxEvent event = new OutboxEvent();

        //赋值
        event.setEventId(UUID.randomUUID().toString());
        event.setAggregateType("ORDER");
        event.setAggregateId(order.getId());
        event.setEventType("ORDER_CREATED");

        try {
            event.setPayload(objectMapper.writeValueAsString(Map.of(
                    "orderId", order.getId(),
                    "orderNo", order.getOrderNo(),
                    "expireAt", order.getExpireAt()
            )));
        } catch (JsonProcessingException e) {
            throw new BizException("订单事件生成失败");
        }

        event.setStatus("PENDING");
        event.setRetryCount(0);
        event.setNextRetryAt(LocalDateTime.now(applicationClock));

        return event;
    }
}

