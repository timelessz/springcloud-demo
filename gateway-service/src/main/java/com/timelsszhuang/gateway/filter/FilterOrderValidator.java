package com.timelsszhuang.gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 过滤器顺序验证工具
 * 用于在启动时打印所有过滤器的执行顺序
 *
 * @author timelsszhuang
 */
@Component
public class FilterOrderValidator {

    private final List<GlobalFilter> globalFilters;

    public FilterOrderValidator(List<GlobalFilter> globalFilters) {
        this.globalFilters = globalFilters;
        printFilterOrder();
    }

    /**
     * 打印过滤器执行顺序
     */
    private void printFilterOrder() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════");
        System.out.println("║ Gateway 过滤器执行顺序");
        System.out.println("╠════════════════════════════════════════════════════════════════");

        // 获取所有实现了 Ordered 接口的过滤器
        List<GlobalFilter> orderedFilters = globalFilters.stream()
                .filter(filter -> filter instanceof Ordered)
                .sorted((f1, f2) -> {
                    int order1 = ((Ordered) f1).getOrder();
                    int order2 = ((Ordered) f2).getOrder();
                    return Integer.compare(order1, order2);
                })
                .collect(Collectors.toList());

        int index = 1;
        for (GlobalFilter filter : orderedFilters) {
            String className = filter.getClass().getSimpleName();
            int order = ((Ordered) filter).getOrder();

            // 只显示我们自定义的过滤器
            if (className.contains("PreLogging") ||
                className.contains("JwtAuthentication") ||
                className.contains("RequestStatistics") ||
                className.contains("LoggingGlobal") ||
                className.contains("PostLogging")) {

                String orderStr = String.format("%4d", order);
                System.out.printf("║ %d. [Order: %s] %s%n", index++, orderStr, className);
            }
        }

        System.out.println("╚════════════════════════════════════════════════════════════════\n");
    }
}

