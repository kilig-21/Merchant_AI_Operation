package org.example.merchant_ai_operation.auth.service;


import org.example.merchant_ai_operation.auth.dto.LoginRequest;
import org.example.merchant_ai_operation.auth.dto.RegisterRequest;
import org.example.merchant_ai_operation.auth.vo.CurrentUserVO;
import org.example.merchant_ai_operation.auth.vo.LoginResponse;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.example.merchant_ai_operation.security.JwtService;
import org.example.merchant_ai_operation.security.LoginPrincipal;
import org.example.merchant_ai_operation.user.SysUser;
import org.example.merchant_ai_operation.user.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    //构造器bean注入:
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    //
    public CurrentUserVO register(RegisterRequest request){

        //先查询
        SysUser existed=userMapper.selectByUsername(request.username());
        if(existed !=null){
            throw new BizException(409, "用户名已存在");
        }

        //这里我们先用 System.currentTimeMillis() 生成 ID，是学习项目里的临时简化做法。
        // 它能让今天的注册闭环跑通；后面如果进入更正式的 ID 方案，比如雪花 ID 或数据库自增，我们再替换
        Long userId = System.currentTimeMillis();
        String passwordHash = passwordEncoder.encode(request.password());

        userMapper.insertConsumer(userId, request.username(), passwordHash);


        //为什么注册后又 selectById 查一次：
        // 因为插入时我们只传了用户名和密码哈希，返回给前端时需要统一走 CurrentUserVO.from(user)，
        // 这样和登录、/me 的返回形状保持一致，而且不会把 passwordHash 暴露出去。
        SysUser  user= userMapper.selectByUsername(request.username());

        return CurrentUserVO.from(user);

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

        //只返回能给用户看的东西:
        CurrentUserVO currentUser = CurrentUserVO.from(user);

        String accessToken=jwtService.createToken(
                user.getId(),
                user.getTenantId(),
                user.getUserType());
        //最后组装:
        return new LoginResponse(accessToken, currentUser);
    }


    public CurrentUserVO me() {
        LoginPrincipal principal = CurrentUser.required();

        SysUser user = userMapper.selectById(principal.userId());
        if (user == null) {
            throw new BizException(401, "登录状态已失效");
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BizException(403, "账号已被禁用");
        }

        return CurrentUserVO.from(user);
    }





}
