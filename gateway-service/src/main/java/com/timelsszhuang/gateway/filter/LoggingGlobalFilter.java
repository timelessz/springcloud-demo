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
 * 全局过滤器 - 记录路由处理日志
 * 在请求路由到后端服务时记录简要信息
 *
 * @author timelsszhuang
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = exchange.getAttribute("REQUEST_ID");

        // 获取目标路由信息（如果存在）
        String targetUri = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl") != null
                ? exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl").toString()
                : "unknown";

        // 记录路由信息（简洁版）
        logger.debug("║ [路由处理] 请求ID: {} | 源路径: {} → 目标: {}",
                requestId, request.getPath(), targetUri);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1; // 在前置和后置过滤器之间
    }

}

