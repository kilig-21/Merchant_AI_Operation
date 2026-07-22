package org.example.merchant_ai_operation.auth.service;


import org.example.merchant_ai_operation.auth.dto.LoginRequest;
import org.example.merchant_ai_operation.auth.vo.CurrentUserVO;
import org.example.merchant_ai_operation.auth.vo.LoginResponse;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.user.SysUser;
import org.example.merchant_ai_operation.user.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    //字段注入:
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request){
        SysUser user =userMapper.selectByUsername(request.username());
        if(user == null){
            throw new BizException(401, "用户名或密码错误");

        }

        if(!Integer.valueOf(1).equals(user.getStatus())){
            throw new BizException(403, "账号已被禁用");
        }

        if(!passwordEncoder.matches(request.password() , user.getPasswordHash())){
            throw new BizException(401, "用户名或密码错误");
        }

        //返回给用户的东西:
        CurrentUserVO currentUser = CurrentUserVO.from(user);

        return new LoginResponse("todo-access-token", currentUser);


    }




}
