package org.example.merchant_ai_operation.order.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.order.dto.CreateCheckoutRequest;
import org.example.merchant_ai_operation.order.dto.CreateOrderRequest;
import org.example.merchant_ai_operation.order.entity.CheckoutGroup;
import org.example.merchant_ai_operation.order.vo.CreateCheckoutGroupVO;
import org.example.merchant_ai_operation.order.vo.CreateOrderVO;
import org.example.merchant_ai_operation.order.vo.OrderSkuSnapshotVO;
import org.example.merchant_ai_operation.security.CurrentUser;
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

    public CheckoutService(
            ProductSkuMapper productSkuMapper,
            CheckoutGroupService checkoutGroupService,
            OrderService orderService
    ) {
        this.productSkuMapper = productSkuMapper;
        this.checkoutGroupService = checkoutGroupService;
        this.orderService = orderService;
    }

    //将商家的订单组成group来清算
    public Map<Long, List<OrderSkuSnapshotVO>> loadGroupedSnapshots(List<Long> cartItemIds) {
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

    //算Group的总金额
    public BigDecimal calculateTotal(Map<Long, List<OrderSkuSnapshotVO>> groupedSnapshots) {
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

    //开始创建团的订单了
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
}
