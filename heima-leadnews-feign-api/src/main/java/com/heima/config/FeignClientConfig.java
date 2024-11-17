package com.heima.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(10000, 30000); // 连接超时10秒，读取超时30秒
    }
}