package org.example.merchant_ai_operation.auth.controller;


import jakarta.validation.Valid;
import org.example.merchant_ai_operation.auth.dto.LoginRequest;
import org.example.merchant_ai_operation.auth.service.AuthService;
import org.example.merchant_ai_operation.auth.vo.LoginResponse;
import org.example.merchant_ai_operation.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest){
        return ApiResponse.ok(authService.login(loginRequest));
    }
}
