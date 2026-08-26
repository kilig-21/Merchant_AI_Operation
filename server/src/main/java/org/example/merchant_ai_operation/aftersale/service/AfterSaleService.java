package org.example.merchant_ai_operation.aftersale.service;

import java.time.Clock;
import org.example.merchant_ai_operation.aftersale.dto.ReviewAfterSaleRequest;
import org.example.merchant_ai_operation.aftersale.dto.SubmitAfterSaleRequest;
import org.example.merchant_ai_operation.aftersale.entity.AfterSaleRequest;
import org.example.merchant_ai_operation.aftersale.entity.AfterSaleStatusLog;
import org.example.merchant_ai_operation.aftersale.mapper.AfterSaleRequestMapper;
import org.example.merchant_ai_operation.aftersale.mapper.AfterSaleStatusLogMapper;
import org.example.merchant_ai_operation.aftersale.vo.AfterSaleOrderItemContext;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderMapper;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;



@Service
public class AfterSaleService {

    private final AfterSaleRequestMapper afterSaleRequestMapper;
    private final AfterSaleStatusLogMapper afterSaleStatusLogMapper;
    private final CommerceOrderMapper commerceOrderMapper;
    private final Clock applicationClock;
    public AfterSaleService(
            AfterSaleRequestMapper afterSaleRequestMapper,
            AfterSaleStatusLogMapper afterSaleStatusLogMapper,
            CommerceOrderMapper commerceOrderMapper,
            Clock applicationClock
    ) {
        this.afterSaleRequestMapper = afterSaleRequestMapper;
        this.afterSaleStatusLogMapper = afterSaleStatusLogMapper;
        this.commerceOrderMapper = commerceOrderMapper;
        this.applicationClock = applicationClock;
    }

    //提交售后记录
    @Transactional
    public AfterSaleRequest submit(SubmitAfterSaleRequest request){
        Long consumerId = CurrentUser.requiredConsumerId();

        //先核对订单的上下文是否正确
        AfterSaleOrderItemContext context = commerceOrderMapper.selectAfterSaleOrderItemContext(
                request.orderItemId(),
                consumerId
        );

        if (context == null) {
            throw new BizException("订单项不存在");
        }

        if (!"PAID".equals(context.orderStatus())) {
            throw new BizException("只有已支付订单可以申请售后");
        }

        if (request.quantity() > context.purchasedQuantity()) {
            throw new BizException("申请数量不能超过购买数量");
        }

        //把要售后的订单金额加进来
        BigDecimal requestedAmount = context.salePrice().multiply(BigDecimal.valueOf(request.quantity()));

        //写进信息
        AfterSaleRequest afterSale = new AfterSaleRequest();
        afterSale.setRequestNo(generateRequestNo());
        afterSale.setOrderId(context.orderId());
        afterSale.setOrderItemId(context.orderItemId());
        afterSale.setTenantId(context.tenantId());
        afterSale.setConsumerId(context.consumerId());
        afterSale.setQuantity(request.quantity());
        afterSale.setRequestedAmount(requestedAmount);
        afterSale.setReason(request.reason());
        afterSale.setStatus("SUBMITTED");

        //插入数据库
        afterSaleRequestMapper.insert(afterSale);

        //把插入售后订单进数据库写进日志里
        writeStatusLog(
                afterSale.getId(),
                null,
                "SUBMITTED",
                consumerId,
                "CONSUMER",
                null
        );
        return afterSale;
    }

    //商家看售后记录
    @Transactional
    public AfterSaleRequest review(Long id, ReviewAfterSaleRequest reviewRequest){

        Long tenantId = CurrentUser.requiredMerchantTenantId();
        LoginPrincipal principal = CurrentUser.required();
        Long operatorId = principal.userId();

        AfterSaleRequest target = afterSaleRequestMapper.selectByIdAndTenantId(id, tenantId);

        if (target == null) {throw new BizException("售后申请不存在");}

        //然后获取状态
        String currentStatus = target.getStatus();

        //检验状态
        if ("SUBMITTED".equals(currentStatus)) {
            transition(
                    target,
                    tenantId,
                    "SUBMITTED",
                    "REVIEWING",
                    operatorId,
                    null,
                    null
            );
            currentStatus = "REVIEWING";
        }

        if (!"REVIEWING".equals(currentStatus)) {
            throw new BizException("当前售后状态不允许审核");
        }

        //最后更新状态
        LocalDateTime decidedAt = LocalDateTime.now(applicationClock);
        int updated =afterSaleRequestMapper.updateStatusByTenantIdAndExpectedStatus(
                id,
                tenantId,
                currentStatus,
                reviewRequest.decision(),
                reviewRequest.remark(),
                operatorId,
                decidedAt
        );
        if (updated != 1) {throw new BizException("售后状态已变化，请重新查询");}


        //把更改状态的日志写进去
        writeStatusLog(
                id,
                currentStatus,
                reviewRequest.decision(),
                operatorId,
                "MERCHANT",
                reviewRequest.remark()
        );
        target.setStatus(reviewRequest.decision());
        target.setMerchantRemark(reviewRequest.remark());
        target.setDecidedBy(operatorId);
        target.setDecidedAt(decidedAt);

        return target;
    }

    //查我的售后列表
    public List<AfterSaleRequest> listMyRequest(){
        return afterSaleRequestMapper.selectByConsumerId(CurrentUser.requiredConsumerId());
    }

    //查某个详细售后订单
    public AfterSaleRequest getMyRequest(Long id) {
        AfterSaleRequest request = afterSaleRequestMapper.selectByIdAndConsumerId(
                id,
                CurrentUser.requiredConsumerId()
                );

        if (request == null) {
            throw new BizException("售后申请不存在");
        }

        return request;
    }

    //列出商家的售后订单列表
    public List<AfterSaleRequest> listMerchantRequests() {
        return afterSaleRequestMapper.selectByTenantId(CurrentUser.requiredMerchantTenantId());
    }

    //查询商家某个售后订单详情
    public AfterSaleRequest getMerchantRequest(Long id) {
        AfterSaleRequest request = afterSaleRequestMapper.selectByIdAndTenantId(
                id,
                CurrentUser.requiredMerchantTenantId()
        );

        if (request == null) {throw new BizException("售后申请不存在");}

        return request;
    }

    public List<AfterSaleOrderItemContext> listEligibleOrderItems() {
        return commerceOrderMapper.selectEligibleAfterSaleItems(
                CurrentUser.requiredConsumerId());
    }


    //<--------------------------私有方法-------------------------->//


    //生成售后订单的number号
    private String generateRequestNo() {
        String datePart = LocalDateTime.now(applicationClock).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        int randomPart = ThreadLocalRandom.current()
                .nextInt(100000, 1000000);

        return "ASR" + datePart + randomPart;
    }

    //写日志方法
    private void writeStatusLog(
            Long afterSaleId,
            String fromStatus,
            String toStatus,
            Long operatorId,
            String operatorType,
            String remark
    ){
        AfterSaleStatusLog log = new AfterSaleStatusLog();
        log.setAfterSaleId(afterSaleId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setOperatorType(operatorType);
        log.setRemark(remark);

        afterSaleStatusLogMapper.insert(log);
    }

    //只有这条申请属于当前商家，并且当前状态仍然是 fromStatus，才允许改成 toStatus。的方法
    private void transition(
            AfterSaleRequest target,
            Long tenantId,
            String fromStatus,
            String toStatus,
            Long operatorId,
            String merchantRemark,
            LocalDateTime decidedAt
    ) {
        int updated =
                afterSaleRequestMapper.updateStatusByTenantIdAndExpectedStatus(
                        target.getId(),
                        tenantId,
                        fromStatus,
                        toStatus,
                        merchantRemark,
                        operatorId,
                        decidedAt
                );

        if (updated != 1) {
            throw new BizException("售后状态已变化，请重新查询");
        }

        writeStatusLog(
                target.getId(),
                fromStatus,
                toStatus,
                operatorId,
                "MERCHANT",
                merchantRemark
        );
    }


}
