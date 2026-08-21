package org.example.merchant_ai_operation.address.service;

import org.example.merchant_ai_operation.address.dto.CreateAddressRequest;
import org.example.merchant_ai_operation.address.dto.UpdateAddressRequest;
import org.example.merchant_ai_operation.address.mapper.ConsumerAddressMapper;
import org.example.merchant_ai_operation.address.vo.ConsumerAddressVO;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsumerAddressService {
    private final ConsumerAddressMapper  consumerAddressMapper;
    public ConsumerAddressService(ConsumerAddressMapper consumerAddressMapper) {
        this.consumerAddressMapper = consumerAddressMapper;
    }

    //列出我的收件地址
    public List<ConsumerAddressVO> listMine(){
        Long consumerId = CurrentUser.requiredConsumerId();
        return consumerAddressMapper.selectByConsumerId(consumerId);
    }

    //创建收件地址
    @Transactional
    public void create(CreateAddressRequest request){
        Long consumerId = CurrentUser.requiredConsumerId();

        if (Boolean.TRUE.equals(request.isDefault())) {
            consumerAddressMapper.clearDefaultByConsumerId(consumerId);
        }

        int inserted = consumerAddressMapper.insert(consumerId, request);

        if (inserted != 1) {
            throw new BizException(500, "新增收货地址失败");
        }
    }

    //修改地址
    @Transactional
    public void update(Long id, UpdateAddressRequest request){
        Long consumerId = CurrentUser.requiredConsumerId();

        if (Boolean.TRUE.equals(request.isDefault())) {
            consumerAddressMapper.clearDefaultByConsumerId(consumerId);
        }

        //更新地址
        int updated = consumerAddressMapper.updateByIdAndConsumerId(
                id,
                consumerId,
                request
        );

        if (updated != 1) {
            throw new BizException(404, "收货地址不存在");
        }

    }

    //删除地址
    @Transactional
    public void delete(Long id){
        Long consumerId = CurrentUser.requiredConsumerId();
        int deleted = consumerAddressMapper.deleteByIdAndConsumerId(id, consumerId);
        if (deleted != 1) {
            throw new BizException(404,"收货地址不存在");
        }
    }

    //查自己的某个地址根据id
    public ConsumerAddressVO getMine(Long id){
        Long consumerId = CurrentUser.requiredConsumerId();

        //查询特定的地址
        ConsumerAddressVO address = consumerAddressMapper.selectByIdAndConsumerId(id, consumerId);

        if (address == null) {
            throw new BizException(404, "收货地址不存在");
        }

        return address;
    }
}
