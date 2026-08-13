package org.example.merchant_ai_operation.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.List;

@Configuration
public class PromotionRedisConfig {

    @Bean
    public DefaultRedisScript<List> promotionReserveScript(){
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/promotion_reserve.lua"));
        script.setResultType(List.class);
        return script;
    }
}
