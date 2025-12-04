package com.timelsszhuang.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 路由配置类（可选，也可以在 application.yml 中配置）
 *
 * @author timelsszhuang
 */
@Configuration
public class GatewayConfig {

    /**
     * 通过代码配置路由（可选）
     * 如果使用 application.yml 配置，可以删除此方法
     */
    // @Bean
    // public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    //     return builder.routes()
    //             // Provider 路由
    //             .route("service-provider", r -> r
    //                     .path("/provider/**")
    //                     .filters(f -> f.stripPrefix(1))
    //                     .uri("lb://service-provider"))
    //             // Consumer 路由
    //             .route("service-consumer", r -> r
    //                     .path("/consumer/**")
    //                     .filters(f -> f.stripPrefix(1))
    //                     .uri("lb://service-consumer"))
    //             .build();
    // }

}

