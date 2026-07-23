package org.example.merchant_ai_operation.config;

import org.example.merchant_ai_operation.security.JwtAuthentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig{

    private final JwtAuthentication jwtAuthentication;
    public SecurityConfig(JwtAuthentication jwtAuthentication) {
        this.jwtAuthentication = jwtAuthentication;
    }



    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
        .csrf(AbstractHttpConfigurer::disable)//把token关了
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
                //意思是：我们不用后端 Session 保存登录状态，每次请求都靠 JWT。
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint((request, response, authException) ->
                                response.setStatus(HttpStatus.UNAUTHORIZED.value())
                        )
                )



        .authorizeHttpRequests(auth->
                auth.requestMatchers(
                    "/api/ping",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/debug/**",
                    "/api/auth/login"
                ).permitAll()
                        //剩下的全部要验证!!!!


                        //匹配前面规则没覆盖到的所有剩余接口
                        .anyRequest()
                        //只允许已登录、身份核验通过的用户访问
                        .authenticated()
        )
        .addFilterBefore(jwtAuthentication, UsernamePasswordAuthenticationFilter.class)
        .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();

    }


}






























//告诉Spring要启动的配置类
//@Configuration
//public class SecurityConfig {
//    @Bean//方法对象交给Spring管理
//    //throws: 如果真的出错，我不在这里处理，交给 Spring Boot 启动流程处理。!!!
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
//        return http
//                //咱们先关闭,因为后端是靠Token来验证登录信息的,所以咱先关闭,避免浏览器拦截
//                .csrf(crsf->crsf.disable())
//                .authorizeHttpRequests(auth->
//                        auth.requestMatchers(
//                                "api/ping",
//                                "/actuator/health",
//                                "/swagger-ui/**",
//                                "/v3/api-docs"
//                                ).permitAll()//同意全部的网站,相当于白名单,
//                                //除了上面放行的地址，其他所有请求都必须登录。
//                                .anyRequest().authenticated()
//                )
//                .build();//把上面写的规则真正生成出来。连起来
//    }
//
//}
//
//
//
//














