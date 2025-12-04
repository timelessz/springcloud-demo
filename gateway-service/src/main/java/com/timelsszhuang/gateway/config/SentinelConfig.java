package com.timelsszhuang.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 配置类
 * 配置限流降级的自定义响应
 *
 * @author timelsszhuang
 */
@Configuration
public class SentinelConfig {

    /**
     * 初始化限流降级的自定义响应处理
     */
    @PostConstruct
    public void init() {
        // 自定义限流降级处理器
        GatewayCallbackManager.setBlockHandler(new CustomBlockRequestHandler());
    }

    /**
     * 自定义限流降级响应处理器
     */
    private static class CustomBlockRequestHandler implements BlockRequestHandler {

        @Override
        public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            result.put("data", null);
            result.put("timestamp", System.currentTimeMillis());

            return ServerResponse
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(result);
        }
    }

}

