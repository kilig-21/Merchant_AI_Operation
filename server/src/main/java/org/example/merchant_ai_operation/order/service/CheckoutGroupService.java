package org.example.merchant_ai_operation.order.service;


import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.order.entity.CheckoutGroup;
import org.example.merchant_ai_operation.order.mapper.CheckoutGroupMapper;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;
import org.example.merchant_ai_operation.order.vo.CheckoutGroupDetailVO;
import org.example.merchant_ai_operation.order.vo.OrderDetailVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Service
//代表“管理结算组这条数据”，属于结算组领域服务。
public class CheckoutGroupService {

    private final CheckoutGroupMapper checkoutGroupMapper;
    private final Clock applicationClock;
    private final CommerceOrderMapper commerceOrderMapper;
    public CheckoutGroupService(
            CheckoutGroupMapper checkoutGroupMapper,
            Clock applicationClock,
            CommerceOrderMapper commerceOrderMapper
    ) {
        this.checkoutGroupMapper = checkoutGroupMapper;
        this.applicationClock = applicationClock;
        this.commerceOrderMapper = commerceOrderMapper;
    }

    //创建自己的结算组
    @Transactional
    public CheckoutGroup createPending(BigDecimal totalAmount){
        if (totalAmount == null || totalAmount.signum() < 0) {
            throw new BizException("结算金额不合法");
        }

        Long consumerId = CurrentUser.requiredConsumerId();
        CheckoutGroup group = new CheckoutGroup();
        //写入变量
        group.setCheckoutNo(generateCheckoutNo());
        group.setConsumerId(consumerId);
        group.setStatus("PENDING_PAYMENT");
        group.setTotalAmount(totalAmount);

        int inserted = checkoutGroupMapper.insert(group);

        if (inserted != 1){
            throw new BizException(500, "创建结算组失败");
        }
        return group;

    }

    //查询自己的结算组
    public CheckoutGroup getMine(Long checkoutGroupId){
        Long consumerId = CurrentUser.requiredConsumerId();
        CheckoutGroup group = checkoutGroupMapper.selectByIdAndConsumerId(
                checkoutGroupId,
                consumerId
        );
        if (group == null) {
            throw new BizException(404, "结算组不存在");
        }
        return group;
    }

    //返回我的组订单详情
    public CheckoutGroupDetailVO getMyDetail(Long checkoutGroupId){
        //首先把我的组查出来:保证父结算组属于当前消费者
        CheckoutGroup group = getMine(checkoutGroupId);
        Long consumerId = CurrentUser.requiredConsumerId();

        List<OrderDetailVO> orders = commerceOrderMapper.selectByCheckoutGroupIdAndConsumerId(
                        checkoutGroupId,
                        consumerId
                )
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
                        List.of(),
                        null
                ))
                .toList();

        return new CheckoutGroupDetailVO(
                group.getId(),
                group.getCheckoutNo(),
                group.getStatus(),
                group.getTotalAmount(),
                group.getCreatedAt(),
                orders
        );

    }



    //创建组订单的号码号
    private String generateCheckoutNo(){
        String datePart = LocalDateTime.now(applicationClock).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        int randomPart = ThreadLocalRandom.current().nextInt(100000, 1000000);

        return "CHK" + datePart + randomPart;
    }

}
