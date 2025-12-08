package com.timelsszhuang.gateway.controller;

import com.timelsszhuang.gateway.filter.RequestStatisticsFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway 管理控制器
 * 提供网关状态查询、统计信息等接口
 *
 * @author timelsszhuang
 */
@RestController
@RequestMapping("/gateway")
public class GatewayManagementController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取网关基本信息
     */
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "gateway-service");
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("currentTime", LocalDateTime.now().format(formatter));
        info.put("description", "Spring Cloud Gateway with Nacos & Sentinel");
        return info;
    }

    /**
     * 获取请求统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now().format(formatter));
        result.put("statistics", RequestStatisticsFilter.getStatistics());
        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(formatter));
        health.put("message", "Gateway is running normally");
        return health;
    }
}

