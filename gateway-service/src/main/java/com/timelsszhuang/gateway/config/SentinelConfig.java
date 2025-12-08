package com.timelsszhuang.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Sentinel 配置类
 * 配置限流降级的自定义响应和流控规则
 *
 * @author timelsszhuang
 */
@Configuration
public class SentinelConfig {

    private static final Logger logger = LoggerFactory.getLogger(SentinelConfig.class);

    /**
     * 初始化限流降级的自定义响应处理和流控规则
     */
    @PostConstruct
    public void init() {
        // 自定义限流降级处理器
        GatewayCallbackManager.setBlockHandler(new CustomBlockRequestHandler());

        // 初始化 API 分组
        initCustomizedApis();

        // 初始化网关流控规则
        initGatewayRules();

        logger.info("Sentinel Gateway 配置初始化完成");
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

    /**
     * 初始化自定义 API 分组
     */
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();

        // Provider API 分组
        ApiDefinition providerApi = new ApiDefinition("provider_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/provider/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(providerApi);

        // Consumer API 分组
        ApiDefinition consumerApi = new ApiDefinition("consumer_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/consumer/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(consumerApi);

        // 认证 API 分组
        ApiDefinition authApi = new ApiDefinition("auth_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/provider/auth/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                    add(new ApiPathPredicateItem().setPattern("/consumer/user/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(authApi);

        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        logger.info("已加载 {} 个 API 分组定义", definitions.size());
    }

    /**
     * 初始化网关流控规则
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // Provider 服务限流规则：每秒最多 10 个请求
        GatewayFlowRule providerRule = new GatewayFlowRule("service-provider")
                .setCount(10)  // 限流阈值
                .setIntervalSec(1);  // 统计时间窗口，单位是秒，默认是 1 秒
        rules.add(providerRule);

        // Consumer 服务限流规则：每秒最多 10 个请求
        GatewayFlowRule consumerRule = new GatewayFlowRule("service-consumer")
                .setCount(10)
                .setIntervalSec(1);
        rules.add(consumerRule);

        // 认证 API 限流规则：每秒最多 5 个请求（防止暴力破解）
        GatewayFlowRule authRule = new GatewayFlowRule("auth_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(5)
                .setIntervalSec(1);
        rules.add(authRule);

        // Provider API 限流规则：每秒最多 20 个请求
        GatewayFlowRule providerApiRule = new GatewayFlowRule("provider_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(20)
                .setIntervalSec(1);
        rules.add(providerApiRule);

        // Consumer API 限流规则：每秒最多 20 个请求
        GatewayFlowRule consumerApiRule = new GatewayFlowRule("consumer_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(20)
                .setIntervalSec(1);
        rules.add(consumerApiRule);

        GatewayRuleManager.loadRules(rules);
        logger.info("已加载 {} 条网关流控规则", rules.size());
    }

}

