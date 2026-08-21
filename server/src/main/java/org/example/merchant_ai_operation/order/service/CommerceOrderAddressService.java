package org.example.merchant_ai_operation.order.service;


import org.example.merchant_ai_operation.address.service.ConsumerAddressService;
import org.example.merchant_ai_operation.address.vo.ConsumerAddressVO;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.order.entity.CommerceOrderAddress;
import org.example.merchant_ai_operation.order.mapper.CommerceOrderAddressMapper;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommerceOrderAddressService {
    private final ConsumerAddressService consumerAddressService;
    private final CommerceOrderAddressMapper commerceOrderAddressMapper;

    public CommerceOrderAddressService(
            ConsumerAddressService consumerAddressService,
            CommerceOrderAddressMapper commerceOrderAddressMapper
    ) {
        this.consumerAddressService = consumerAddressService;
        this.commerceOrderAddressMapper = commerceOrderAddressMapper;
    }

    @Transactional
    public void createSnapshot(Long orderId,Long sourceAddressId){
        Long consumerId = CurrentUser.requiredConsumerId();

        //先查询我的
        ConsumerAddressVO source = consumerAddressService.getMine(sourceAddressId);

        //写进数据库
        CommerceOrderAddress snapshot = buildOrderAddressSnapshot(orderId, consumerId, source);

        int inserted = commerceOrderAddressMapper.insert(snapshot);

        if (inserted != 1) {
            throw new BizException(500, "创建订单地址快照失败");
        }
    }

    //方法提取:把快照写进数据库内
    private static CommerceOrderAddress buildOrderAddressSnapshot(Long orderId, Long consumerId, ConsumerAddressVO source) {
        CommerceOrderAddress snapshot = new CommerceOrderAddress();
        snapshot.setOrderId(orderId);
        snapshot.setConsumerId(consumerId);
        snapshot.setSourceAddressId(source.id());
        snapshot.setReceiverName(source.receiverName());
        snapshot.setReceiverPhone(source.receiverPhone());
        snapshot.setProvince(source.province());
        snapshot.setCity(source.city());
        snapshot.setDistrict(source.district());
        snapshot.setDetailAddress(source.detailAddress());
        return snapshot;
    }


}
