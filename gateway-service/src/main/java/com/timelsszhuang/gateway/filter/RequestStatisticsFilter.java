package com.timelsszhuang.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求统计过滤器 - 统计请求次数和耗时
 *
 * @author timelsszhuang
 */
@Component
public class RequestStatisticsFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestStatisticsFilter.class);

    // 统计各路径的请求次数
    private static final Map<String, AtomicLong> REQUEST_COUNT = new ConcurrentHashMap<>();

    // 统计总请求次数
    private static final AtomicLong TOTAL_REQUEST_COUNT = new AtomicLong(0);

    // 统计各路径的总耗时
    private static final Map<String, AtomicLong> TOTAL_DURATION = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        long startTime = System.currentTimeMillis();

        // 增加请求计数
        REQUEST_COUNT.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();
        long totalCount = TOTAL_REQUEST_COUNT.incrementAndGet();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;

            // 累加耗时
            TOTAL_DURATION.computeIfAbsent(path, k -> new AtomicLong(0)).addAndGet(duration);

            // 每100个请求打印一次统计信息
            if (totalCount % 100 == 0) {
                printStatistics();
            }
        }));
    }

    /**
     * 打印统计信息
     */
    private void printStatistics() {
        logger.info("════════════════════════════════════════════════════════════════");
        logger.info("【Gateway 请求统计】");
        logger.info("════════════════════════════════════════════════════════════════");
        logger.info("总请求次数: {}", TOTAL_REQUEST_COUNT.get());
        logger.info("────────────────────────────────────────────────────────────────");
        logger.info("各路径统计:");
        REQUEST_COUNT.forEach((path, count) -> {
            long totalDuration = TOTAL_DURATION.getOrDefault(path, new AtomicLong(0)).get();
            long avgDuration = count.get() > 0 ? totalDuration / count.get() : 0;
            logger.info("  路径: {}", path);
            logger.info("    请求次数: {} 次", count.get());
            logger.info("    平均耗时: {} ms", avgDuration);
            logger.info("  ────────────────────────────────────────────────────────");
        });
        logger.info("════════════════════════════════════════════════════════════════");
    }

    /**
     * 获取统计信息（供外部调用）
     */
    public static Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalRequestCount", TOTAL_REQUEST_COUNT.get());
        stats.put("pathStatistics", REQUEST_COUNT);
        stats.put("pathDurations", TOTAL_DURATION);
        return stats;
    }

    @Override
    public int getOrder() {
        return 0; // 中等优先级
    }
}

