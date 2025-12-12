package com.timelsszhuang.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * CORS 跨域配置
 * 
 * 配置 Gateway 允许跨域请求，优先级设置为最高，
 * 确保在 Sentinel 和其他过滤器之前处理 OPTIONS 预检请求
 *
 * @author timelsszhuang
 */
@Configuration
public class CorsConfig {

    /**
     * CORS 跨域过滤器
     * 
     * 使用 CorsWebFilter 配置跨域策略
     * Order 设置为 Ordered.HIGHEST_PRECEDENCE 确保优先执行
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源（生产环境应指定具体域名）
        config.addAllowedOriginPattern("*");

        // 允许的请求方法
        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.OPTIONS.name()));

        // 允许的请求头
        config.addAllowedHeader("*");

        // 允许携带凭证（Cookie）
        config.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        // 暴露的响应头（允许前端访问）
        config.setExposedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                "X-User-Name"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用 CORS 配置
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    /**
     * OPTIONS 预检请求过滤器
     * 
     * 确保 OPTIONS 请求直接返回，不经过后续的认证和限流过滤器
     * 优先级设置为最高
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsPreflightFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 如果是 OPTIONS 预检请求，直接返回成功
            if (HttpMethod.OPTIONS.equals(request.getMethod())) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

            return chain.filter(exchange);
        };
    }
}
