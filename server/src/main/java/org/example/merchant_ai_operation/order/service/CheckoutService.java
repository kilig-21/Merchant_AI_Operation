package org.example.merchant_ai_operation.order.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.idempotency.entity.IdempotentRequest;
import org.example.merchant_ai_operation.idempotency.mapper.IdempotentRequestMapper;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.order.dto.CreateCheckoutRequest;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.entity.CheckoutGroup;
import org.example.merchant_ai_operation.order.vo.CheckoutGroupDetailVO;
import org.example.merchant_ai_operation.order.vo.CreateCheckoutGroupVO;
import org.example.merchant_ai_operation.order.vo.CreateOrderVO;
import org.example.merchant_ai_operation.order.vo.OrderSkuSnapshotVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
//代表“执行一次结算流程”，属于流程编排层。
public class CheckoutService {
    private final ProductSkuMapper productSkuMapper;
    private final CheckoutGroupService checkoutGroupService;
    private final OrderService orderService;
    private final IdempotentRequestMapper idempotentRequestMapper;

    public CheckoutService(
            ProductSkuMapper productSkuMapper,
            CheckoutGroupService checkoutGroupService,
            OrderService orderService,
            IdempotentRequestMapper idempotentRequestMapper
    ) {
        this.productSkuMapper = productSkuMapper;
        this.checkoutGroupService = checkoutGroupService;
        this.orderService = orderService;
        this.idempotentRequestMapper = idempotentRequestMapper;
    }


    // 最外层事务：任一子订单失败时，父结算组和已创建的子订单一同回滚。
    @Transactional
    public CreateCheckoutGroupVO submitCheckout(String idempotencyKey, CreateCheckoutRequest request){

        //先校验请求是否合法 -> 防止空购物车提交
        if (idempotencyKey == null || idempotencyKey.isBlank()) {throw new BizException(400, "Idempotency-Key 不能为空");}
        if (request == null || request.addressId() == null) {throw new BizException(400, "请选择收货地址");}
        if (request.cartItemIds() == null || request.cartItemIds().isEmpty()) {throw new BizException(400, "请选择要结算的购物车项");}

        //获得身份和请求的唯一哈希值
        Long consumerId = CurrentUser.requiredConsumerId();
        //生成请求指纹
        String requestHash = buildCheckoutRequestHash(request);

        //查询父级幂等记录
        IdempotentRequest existing = idempotentRequestMapper.selectByConsumerIdAndRequestKey(
                consumerId,
                idempotencyKey
        );

        if (existing != null) {
            if (!requestHash.equals(existing.getRequestHash())) {
                throw new BizException(409, "同一个 Idempotency-Key 不能用于不同结算参数");
            }

            //说明第一次请求已经成功，只是前端可能因为网络问题没有收到响应
            if ("SUCCESS".equals(existing.getStatus())) {
                if (existing.getCheckoutGroupId() == null) {
                    throw new BizException(409, "幂等键已用于非结算组下单");
                }

                return rebuildSuccessfulCheckout(existing.getCheckoutGroupId());
            }

            throw new BizException(409, "结算请求正在处理中，请稍后查询结算结果");
        }

        //之前existing没有key的情况下,也就是第一次进来
        IdempotentRequest parentRequest = new IdempotentRequest();
        parentRequest.setConsumerId(consumerId);
        parentRequest.setRequestKey(idempotencyKey);
        parentRequest.setRequestHash(requestHash);
        parentRequest.setStatus("PROCESSING");

        try {
            idempotentRequestMapper.insert(parentRequest);
        } catch (DuplicateKeyException exception) {
            throw new BizException(409, "结算请求正在处理中，请稍后查询结算结果");
        }

        //创建父结算组
        CreateCheckoutGroupVO pendingCheckout = createPendingCheckout(request);

        //按商家创建子订单
        CreateCheckoutGroupVO result = createChildOrders(
                pendingCheckout.checkoutGroupId(),
                idempotencyKey,
                request
        );

        //全部完成后标记成功
        int updated = idempotentRequestMapper.markCheckoutGroupSuccess(
                parentRequest.getId(),
                result.checkoutGroupId()
        );

        //失败就回滚
        if (updated != 1) {
            throw new BizException(500, "更新结算组幂等记录失败");
        }

        return result;

    }

    @Transactional
    public void mockPayCheckoutGroup(Long checkoutGroupId) {
        //先拿自己的组
        CheckoutGroupDetailVO detail = checkoutGroupService.getMyDetail(checkoutGroupId);

        if (detail.orders().isEmpty()) {
            throw new BizException(409, "结算组没有可支付的子订单");
        }

        for (var order : detail.orders()) {
            //这个支付了的就跳过
            if ("PAID".equals(order.status())) {continue;}

            if (!"PENDING_PAYMENT".equals(order.status())) {
                throw new BizException(409, "结算组存在不可支付的子订单");
            }
            orderService.mockPay(order.id());
        }

        //最后又再一次检查一遍没问题了就把父组标记为成功
        checkoutGroupService.markPaidIfAllChildrenPaid(checkoutGroupId);
    }

    @Transactional
    public void cancelCheckoutGroup(Long checkoutGroupId) {
        CheckoutGroupDetailVO detail = checkoutGroupService.getMyDetail(checkoutGroupId);

        if (detail.orders().isEmpty()) {
            throw new BizException(409, "结算组没有可取消的子订单");
        }

        for (var order : detail.orders()) {
            if ("CANCELLED".equals(order.status())) {
                continue;
            }

            if (!"PENDING_PAYMENT".equals(order.status())) {
                throw new BizException(409, "结算组存在不可取消的子订单");
            }

            orderService.cancelOrder(order.id());
        }

        checkoutGroupService.markCancelledIfAllChildrenCancelled(checkoutGroupId);
    }

    //开始创建团的订单了:它会再次查询购物车快照，并按商家循环：
    @Transactional
    public CreateCheckoutGroupVO createChildOrders(Long checkoutGroupId, String idempotencyKey, CreateCheckoutRequest request) {
        if (request == null || request.addressId() == null) {
            throw new BizException(400, "请选择地址!");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BizException(400, "Idempotency-Key 不能为空");
        }
        //找到我的结算组
        CheckoutGroup group = checkoutGroupService.getMine(checkoutGroupId);
        if (group == null) {
            throw new BizException(400, "请选择你的结算组!");
        }

        //准备结算组草案
        Map<Long, List<OrderSkuSnapshotVO>> groupedSnapshots = loadGroupedSnapshots(request.cartItemIds());

        //算结算组总金额
        BigDecimal currentTotal = calculateTotal(groupedSnapshots);

        if (currentTotal.compareTo(group.getTotalAmount()) != 0) {
            throw new BizException(409, "购物车金额已发生变化，请重新准备结算");
        }

        //创建Order的数组
        List<CreateOrderVO> orders = new java.util.ArrayList<>();

        //写进数据:
        for (Map.Entry<Long, List<OrderSkuSnapshotVO>> entry : groupedSnapshots.entrySet()) {

            List<Long> childCartItemIds = entry.getValue()
                    .stream()
                    .map(OrderSkuSnapshotVO::cartItemId)
                    .toList();

            CreateOrderRequest childRequest = new CreateOrderRequest(
                    childCartItemIds,
                    request.addressId()
            );

            //创建幂等key
            String childIdempotencyKey = idempotencyKey + ":" + entry.getKey();

            orders.add(orderService.createOrderVO(
                            childIdempotencyKey,
                            childRequest,
                            checkoutGroupId
                    )
            );
        }

        return new CreateCheckoutGroupVO(
                group.getId(),
                group.getCheckoutNo(),
                group.getStatus(),
                group.getTotalAmount(),
                orders
        );
    }

    //创建一次结算草稿/结算总组
    @Transactional
    public CreateCheckoutGroupVO createPendingCheckout(CreateCheckoutRequest request) {
        if (request == null || request.addressId() == null) {
            throw new BizException(400, "请选择收货地址");
        }

        Map<Long, List<OrderSkuSnapshotVO>> groupedSnapshots = loadGroupedSnapshots(request.cartItemIds());

        BigDecimal totalAmount = calculateTotal(groupedSnapshots);

        CheckoutGroup group = checkoutGroupService.createPending(totalAmount);

        return new CreateCheckoutGroupVO(
                group.getId(),
                group.getCheckoutNo(),
                group.getStatus(),
                group.getTotalAmount(),
                List.of()
        );
    }


    //<------------------------------ 包内测试辅助方法 --------------------------------->


    //算Group的总金额
    BigDecimal calculateTotal(Map<Long, List<OrderSkuSnapshotVO>> groupedSnapshots) {
        if (groupedSnapshots == null || groupedSnapshots.isEmpty()) {
            throw new BizException(400, "没有可结算的商品");
        }
        return groupedSnapshots.values()
                .stream()
                .flatMap(List::stream)
                .map(snapshot ->
                        snapshot.salePrice()
                                .multiply(BigDecimal.valueOf(snapshot.quantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //将商家的订单组成group来清算
    Map<Long, List<OrderSkuSnapshotVO>> loadGroupedSnapshots(List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BizException(400, "请选择要结算的购物车项");
        }

        Long consumerId = CurrentUser.requiredConsumerId();
        List<OrderSkuSnapshotVO> snapshots = productSkuMapper.selectOrderSkuSnapshots(
                consumerId,
                cartItemIds
        );
        if (snapshots.size() != cartItemIds.size()) {
            throw new BizException("购物车项不存在");
        }

        return snapshots.stream().collect(Collectors.groupingBy(
                OrderSkuSnapshotVO::tenantId,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    //让“相同购物车项但数组顺序不同”仍视作同一请求
    private String buildCheckoutRequestHash(CreateCheckoutRequest request){

        String cartItemPart = request.cartItemIds()
                .stream()
                .sorted()
                .map(String::valueOf)   //等价于:id -> String.valueOf(id)
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        return cartItemPart + "|addressId=" + request.addressId();
    }

    //让重试请求直接复用已经成功的结算组，不再创建新父组和子订单。
    private CreateCheckoutGroupVO rebuildSuccessfulCheckout(Long checkoutGroupId) {
        CheckoutGroupDetailVO detail = checkoutGroupService.getMyDetail(checkoutGroupId);

        List<CreateOrderVO> orders = detail.orders()
                .stream()
                .map(order -> new CreateOrderVO(
                        order.id(),
                        order.orderNo(),
                        order.status(),
                        order.totalAmount(),
                        order.expireAt()
                ))
                .toList();

        return new CreateCheckoutGroupVO(
                detail.checkoutGroupId(),
                detail.checkoutNo(),
                detail.status(),
                detail.totalAmount(),
                orders
        );
    }

}
