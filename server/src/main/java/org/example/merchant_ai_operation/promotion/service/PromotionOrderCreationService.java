package org.example.merchant_ai_operation.promotion.service;

import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.merchant.product.entity.ProductSku;
import org.example.merchant_ai_operation.merchant.product.mapper.ProductSkuMapper;
import org.example.merchant_ai_operation.order.entity.CommerceOrderItem;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderItemMapper;
import org.example.merchant_ai_operation.promotion.entity.PromotionItem;
import org.example.merchant_ai_operation.promotion.entity.PromotionReservation;
import org.example.merchant_ai_operation.promotion.mapper.PromotionItemMapper;
import org.example.merchant_ai_operation.promotion.mapper.PromotionReservationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.merchant_ai_operation.order.entity.CommerceOrder;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PromotionOrderCreationService {

    private final PromotionReservationMapper promotionReservationMapper;
    private final CommerceOrderMapper commerceOrderMapper;
    private final Clock applicationClock;
    private final PromotionItemMapper promotionItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final CommerceOrderItemMapper commerceOrderItemMapper;

    public PromotionOrderCreationService(
            PromotionReservationMapper promotionReservationMapper,
            CommerceOrderMapper commerceOrderMapper,
            Clock applicationClock,
            PromotionItemMapper promotionItemMapper,
            ProductSkuMapper productSkuMapper,
            CommerceOrderItemMapper commerceOrderItemMapper
    ) {
        this.promotionReservationMapper = promotionReservationMapper;
        this.commerceOrderMapper = commerceOrderMapper;
        this.applicationClock = applicationClock;
        this.promotionItemMapper = promotionItemMapper;
        this.productSkuMapper = productSkuMapper;
        this.commerceOrderItemMapper = commerceOrderItemMapper;
    }

    @Transactional
    public void createOrderFromReservation(String reservationId) {
        PromotionReservation reservation =
                promotionReservationMapper
                        .selectByReservationIdForUpdate(reservationId);

        if (reservation == null) {
            throw new BizException(404, "抢购资格不存在");
        }

        if ("ORDER_CREATED".equals(reservation.getStatus())) {
            return;
        }

        if (!"PENDING_ORDER".equals(reservation.getStatus())) {
            throw new BizException(409, "抢购资格当前状态不能创建订单");
        }

        if (promotionItemMapper.deductAvailableStockForReservation(
                reservation.getActivityItemId(),
                reservation.getTenantId(),
                reservation.getQuantity()) != 1 /*看是否更改了一行*/ ) {
            throw new BizException(409, "活动库存不足或状态不一致");
        }


        //实际创建订单主表对象
        CommerceOrder order = createPendingPaymentOrder(reservation);
        if (commerceOrderMapper.insert(order) != 1) {
            throw new BizException(500, "创建促销订单失败");
        }

        //实际创建订单明细对象
        CommerceOrderItem orderItem =
                createPromotionOrderItem(reservation, order);

        if (commerceOrderItemMapper.insert(orderItem) != 1) {
            throw new BizException(500, "创建促销订单明细失败");
        }
        if (promotionReservationMapper.markOrderCreated(
                reservation.getReservationId(), order.getId()) != 1) {
            throw new BizException(500, "更新抢购资格订单状态失败");
        }
    }

    //创建订单
    private CommerceOrder createPendingPaymentOrder(PromotionReservation reservation) {
        CommerceOrder order = new CommerceOrder();
        order.setOrderNo(generatePromotionOrderNo());
        order.setTenantId(reservation.getTenantId());
        order.setConsumerId(reservation.getConsumerId());
        order.setStatus("PENDING_PAYMENT");
        order.setTotalAmount(
                reservation.getUnitPriceSnapshot()
                        .multiply(BigDecimal.valueOf(reservation.getQuantity()))
        );
        order.setExpireAt(
                LocalDateTime.now(applicationClock).plusMinutes(30)
        );
        return order;
    }

    //创建订单详情
    private CommerceOrderItem createPromotionOrderItem(PromotionReservation reservation, CommerceOrder order) {
        PromotionItem promotionItem = promotionItemMapper.selectById(
                reservation.getActivityItemId()
        );
        if (promotionItem == null) {
            throw new BizException(500, "促销活动商品不存在");
        }

        ProductSku sku = productSkuMapper.selectByIdAndTenantId(
                promotionItem.getSkuId(),
                reservation.getTenantId()
        );
        if (sku == null) {
            throw new BizException(500, "促销订单对应 SKU 不存在");
        }

        CommerceOrderItem orderItem = new CommerceOrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setSkuNameSnapshot(sku.getSkuName());

        // 成交价必须使用资格获得时的活动价，不能读取当前普通售价。
        orderItem.setSalePrice(reservation.getUnitPriceSnapshot());
        orderItem.setQuantity(reservation.getQuantity());
        return orderItem;
    }

    private String generatePromotionOrderNo() {
        String datePart = LocalDateTime.now(applicationClock)
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current()
                .nextInt(100000, 1000000);
        return "PROMO" + datePart + randomPart;
    }


}