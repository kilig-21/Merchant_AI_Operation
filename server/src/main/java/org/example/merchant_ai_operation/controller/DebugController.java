package org.example.merchant_ai_operation.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.example.merchant_ai_operation.common.BizException;
import org.springframework.web.bind.annotation.*;

@RestController    //交给spring管理:@RestController = @Controller + @ResponseBody
@RequestMapping("/api/debug")//匹配下方所有方法的父路径
public class DebugController {

    @GetMapping("/biz-error")
    public ApiResponse<Void> bizError(){
        throw new BizException("这是一个业务异常测试");
    }
    @GetMapping("/system-error")
    public ApiResponse<Void> systemError(){
        throw new RuntimeException("这是一个系统异常测试");
    }


    @PostMapping("/validate")
                                        //@valid表示让这个DTO里的notblank和min生效
                                                //@RequestBody表示从请求 JSON 里读取数据。
    public ApiResponse<String> validate(@Valid @RequestBody DebugValidateRequest request){

        return ApiResponse.ok("参数校验通过: "+request.name()+",数量: "+request.quantity());
    }

}
