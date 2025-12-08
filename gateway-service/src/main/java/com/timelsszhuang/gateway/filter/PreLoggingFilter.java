package com.timelsszhuang.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 前置过滤器 - 请求进入时记录日志
 *
 * @author timelsszhuang
 */
@Component
public class PreLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(PreLoggingFilter.class);
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String REQUEST_START_TIME = "REQUEST_START_TIME";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 生成请求ID
        String requestId = UUID.randomUUID().toString().replace("-", "");

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 将请求ID和开始时间存储到 exchange 的 attributes 中
        exchange.getAttributes().put(REQUEST_ID, requestId);
        exchange.getAttributes().put(REQUEST_START_TIME, startTime);

        // 获取请求信息
        String path = request.getPath().value();
        String method = request.getMethod().toString();
        String uri = request.getURI().toString();
        HttpHeaders headers = request.getHeaders();
        String remoteAddress = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        // 记录前置日志
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ [前置过滤器] 请求进入");
        logger.info("╠════════════════════════════════════════════════════════════════");
        logger.info("║ 请求ID      : {}", requestId);
        logger.info("║ 请求时间    : {}", LocalDateTime.now().format(formatter));
        logger.info("║ 请求方法    : {}", method);
        logger.info("║ 请求路径    : {}", path);
        logger.info("║ 完整URI     : {}", uri);
        logger.info("║ 客户端IP    : {}", remoteAddress);
        logger.info("║ 请求参数    : {}", request.getQueryParams());
        logger.info("║ Content-Type: {}", headers.getContentType());
        logger.info("║ User-Agent  : {}", headers.getFirst(HttpHeaders.USER_AGENT));
        logger.info("╚════════════════════════════════════════════════════════════════");

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级，第一个执行
    }
}


