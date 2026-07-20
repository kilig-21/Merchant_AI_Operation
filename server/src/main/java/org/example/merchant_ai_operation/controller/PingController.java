package org.example.merchant_ai_operation.controller;

import org.example.merchant_ai_operation.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api")
public class PingController{
    @GetMapping("/ping")
    public ApiResponse<String> ping(){
        return ApiResponse.ok("pong");
    }
}
