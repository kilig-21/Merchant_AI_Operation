package org.example.merchant_ai_operation.common;




//RequestLogFilter 负责记录每次 HTTP 请求的方法、路径、状态码和耗时


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@Slf4j
//把这个类交给 Spring 管理。这样 Spring Boot 启动后会自动发现这个过滤器。
public class RequestLogFilter extends OncePerRequestFilter {//OncePerRequestFilter表示“每个请求只执行一次”的过滤器。它会包在 Controller 外面。

    @Override//重写里面的方法
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response ,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {//抛出了两个异常


        long start=System.currentTimeMillis();

        try {
            //意思是：放行请求，让它继续往后走，进入 Security、Controller、异常处理等后续流程。
            log.info("RequestLogFilter start");
            filterChain.doFilter(request, response);
        }finally {
            long cost = System.currentTimeMillis() - start;//现在的时间减去开始时记录的时间
            
            log.info(
                    "请求完成 method={} url={} status={} cost={}ms",
                    request.getMethod(),
                    request.getRequestURL(),
                    response.getStatus(),
                    cost
            );
        }
    }

}
