package com.timelsszhuang.provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RefreshScope  // 支持Nacos配置动态刷新
public class ProviderController {

    @Value("${server.port}")
    private String port;

    @Value("${provider.message:Hello from Provider}")
    private String message;

    @Value("${provider.version:1.0}")
    private String version;

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("port", port);
        result.put("version", version);
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "service-provider");
        return result;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("message", message);
        config.put("version", version);
        config.put("port", port);
        return config;
    }

}

