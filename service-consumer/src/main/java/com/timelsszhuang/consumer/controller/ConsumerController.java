package com.timelsszhuang.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RefreshScope  // 支持Nacos配置动态刷新
public class ConsumerController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${server.port}")
    private String port;

    @Value("${consumer.message:Hello from Consumer}")
    private String message;

    @Value("${consumer.version:1.0}")
    private String version;

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("port", port);
        result.put("version", version);
        result.put("service", "service-consumer");
        return result;
    }

    @GetMapping("/call-provider")
    public Map<String, Object> callProvider() {
        // 使用服务名称调用服务提供者
        String url = "http://service-provider/api/hello";
        Map<String, Object> providerResponse = restTemplate.getForObject(url, Map.class);

        Map<String, Object> result = new HashMap<>();
        result.put("consumer", message);
        result.put("consumerPort", port);
        result.put("consumerVersion", version);
        result.put("providerResponse", providerResponse);
        return result;
    }

    @GetMapping("/services")
    public Map<String, Object> getServices() {
        List<String> services = discoveryClient.getServices();
        Map<String, Object> result = new HashMap<>();
        result.put("services", services);

        // 获取service-provider的实例信息
        List<ServiceInstance> instances = discoveryClient.getInstances("service-provider");
        result.put("providerInstances", instances);

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

