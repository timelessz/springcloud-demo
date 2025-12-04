package com.timelsszhuang.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器 - 记录请求日志
 *
 * @author timelsszhuang
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 记录请求信息
        logger.info("========================================");
        logger.info("Gateway 请求路径: {}", request.getPath());
        logger.info("请求方法: {}", request.getMethod());
        logger.info("请求参数: {}", request.getQueryParams());
        logger.info("客户端地址: {}", request.getRemoteAddress());
        logger.info("========================================");

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 计算请求耗时
            long duration = System.currentTimeMillis() - startTime;
            logger.info("请求完成，耗时: {} ms", duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // 优先级最高
    }

}

