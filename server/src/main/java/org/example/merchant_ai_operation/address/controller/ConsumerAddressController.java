package org.example.merchant_ai_operation.address.controller;

import jakarta.validation.Valid;
import org.example.merchant_ai_operation.address.dto.CreateAddressRequest;
import org.example.merchant_ai_operation.address.dto.UpdateAddressRequest;
import org.example.merchant_ai_operation.address.service.ConsumerAddressService;
import org.example.merchant_ai_operation.address.vo.ConsumerAddressVO;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class ConsumerAddressController {
    private final ConsumerAddressService consumerAddressService;

    public ConsumerAddressController(
            ConsumerAddressService consumerAddressService
    ) {
        this.consumerAddressService = consumerAddressService;
    }

    //返回我的地址的接口
    @GetMapping
    public ApiResponse<List<ConsumerAddressVO>> listMine() {
        return ApiResponse.ok(consumerAddressService.listMine());
    }

    //创建收件地址的接口
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateAddressRequest request) {
        consumerAddressService.create(request);
        return ApiResponse.ok(null);
    }

    //更新地址
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        consumerAddressService.update(id, request);
        return ApiResponse.ok(null);
    }

    //删除地址
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        consumerAddressService.delete(id);
        return ApiResponse.ok(null);
    }

}
