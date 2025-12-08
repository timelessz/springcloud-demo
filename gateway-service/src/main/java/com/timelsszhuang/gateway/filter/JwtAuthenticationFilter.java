package com.timelsszhuang.gateway.filter;

import com.timelsszhuang.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器
 * 对请求进行 JWT Token 验证
 *
 * @author timelsszhuang
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 白名单路径 - 不需要 JWT 认证的路径
     */
    private static final List<String> WHITELIST_PATHS = Arrays.asList(
            "/provider/auth/login",
            "/provider/auth/register",
            "/consumer/user/login",
            "/consumer/user/register",
            "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 检查是否在白名单中
        if (isWhiteListPath(path)) {
            logger.debug("白名单路径，跳过JWT验证: {}", path);
            return chain.filter(exchange);
        }

        // 从请求头中获取 Token
        String authHeader = request.getHeaders().getFirst(JwtUtil.HEADER_STRING);

        if (authHeader == null || authHeader.isEmpty()) {
            logger.warn("请求头中没有找到 Authorization: {}", path);
            return unauthorizedResponse(exchange, "未提供认证令牌");
        }

        // 提取 Token
        String token = jwtUtil.extractToken(authHeader);
        if (token == null) {
            logger.warn("无效的 Token 格式: {}", path);
            return unauthorizedResponse(exchange, "无效的令牌格式");
        }

        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            logger.warn("Token 验证失败: {}", path);
            return unauthorizedResponse(exchange, "令牌验证失败或已过期");
        }

        // Token 验证通过，从 Token 中提取用户信息并添加到请求头
        try {
            String username = jwtUtil.getUsernameFromToken(token);

            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            logger.debug("JWT 验证通过，用户: {}, 路径: {}", username, path);

            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            logger.error("处理 Token 时发生错误: {}", e.getMessage());
            return unauthorizedResponse(exchange, "令牌处理失败");
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        return WHITELIST_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
            "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
            message,
            System.currentTimeMillis()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 优先级设置为 -100，确保在其他过滤器之前执行
        return -100;
    }
}

