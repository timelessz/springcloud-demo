package com.timelsszhuang.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 后置过滤器 - 响应返回时记录日志
 *
 * @author timelsszhuang
 */
@Component
public class PostLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(PostLoggingFilter.class);
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String REQUEST_START_TIME = "REQUEST_START_TIME";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 从 attributes 中获取请求ID和开始时间
            String requestId = exchange.getAttribute(REQUEST_ID);
            Long startTime = exchange.getAttribute(REQUEST_START_TIME);

            ServerHttpResponse response = exchange.getResponse();
            HttpStatusCode statusCode = response.getStatusCode();

            // 计算请求耗时
            long duration = 0;
            if (startTime != null) {
                duration = System.currentTimeMillis() - startTime;
            }

            // 获取响应信息
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().toString();

            // 根据响应状态码选择不同的日志级别
            String statusEmoji = getStatusEmoji(statusCode);

            // 记录后置日志
            logger.info("╔════════════════════════════════════════════════════════════════");
            logger.info("║ [后置过滤器] 响应返回 {}", statusEmoji);
            logger.info("╠════════════════════════════════════════════════════════════════");
            logger.info("║ 请求ID      : {}", requestId);
            logger.info("║ 响应时间    : {}", LocalDateTime.now().format(formatter));
            logger.info("║ 请求方法    : {}", method);
            logger.info("║ 请求路径    : {}", path);
            logger.info("║ 响应状态    : {}", statusCode != null ? statusCode.value() : "unknown");
            logger.info("║ 请求耗时    : {} ms", duration);
            logger.info("║ Content-Type: {}", response.getHeaders().getContentType());

            // 性能警告
            if (duration > 3000) {
                logger.warn("║ ⚠️  性能警告  : 请求耗时超过 3 秒！");
            } else if (duration > 1000) {
                logger.warn("║ ⚠️  性能提示  : 请求耗时超过 1 秒");
            }

            logger.info("╚════════════════════════════════════════════════════════════════");

            // 如果是错误响应，记录错误日志
            if (statusCode != null && statusCode.isError()) {
                logger.error("请求失败! 请求ID: {}, 路径: {}, 状态码: {}",
                        requestId, path, statusCode.value());
            }
        }));
    }

    /**
     * 根据状态码返回对应的表情符号
     */
    private String getStatusEmoji(HttpStatusCode statusCode) {
        if (statusCode == null) {
            return "❓";
        }
        if (statusCode.is2xxSuccessful()) {
            return "✅";
        } else if (statusCode.is3xxRedirection()) {
            return "↪️";
        } else if (statusCode.is4xxClientError()) {
            return "⚠️";
        } else if (statusCode.is5xxServerError()) {
            return "❌";
        }
        return "❓";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // 最低优先级，最后执行
    }
}

