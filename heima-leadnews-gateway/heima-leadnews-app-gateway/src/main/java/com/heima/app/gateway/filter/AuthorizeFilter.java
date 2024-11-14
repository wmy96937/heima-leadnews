package com.heima.app.gateway.filter;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.heima.app.gateway.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author ASUS
 * @date 2024-11-11 10:29
 **/
@Component
@Slf4j
public class AuthorizeFilter implements Ordered , GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();

        if(request.getURI().getPath().contains("/login")){
            // 放行
            return chain.filter(exchange);
        }

        //获得token
        String token = request.getHeaders().getFirst("token");

        //判断是否存在
        if(StringUtils.isBlank(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }


        //判断是否有效
        try {
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            //是否过期
            int result = AppJwtUtil.verifyToken(claimsBody);
            if(result==1 || result==2){
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //放行
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
