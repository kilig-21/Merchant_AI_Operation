package org.example.merchant_ai_operation.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component


//OncePerRequestFilter 意思是：这是一个“每个请求只执行一次”的过滤器
public class JwtAuthentication  extends OncePerRequestFilter {

    //构造器注入:
    private final JwtService jwtService;
    public JwtAuthentication(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
             HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);//<==>request.getHeader("Authorization")等价


        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                LoginPrincipal principal = jwtService.parse(token);//拿去校验


                //权限列表:映射到下方的一个用户可以有多个角色/权限
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + principal.userType())
                );

                //创建用户的权限
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                //setAuthentication : 放进当前请求的 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }catch (Exception e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());//value() 是在把枚举里的状态码数字取出来。
                //表示这个请求到此结束，不继续往后走 Controller。
                return;
            }
        }
        filterChain.doFilter(request, response);
    }



}
