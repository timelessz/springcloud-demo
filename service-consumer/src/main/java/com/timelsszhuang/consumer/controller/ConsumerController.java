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
    
    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("port", port);
        result.put("version", version);
        result.put("service", "service-consumer");
        return result;
    }

    /**
     * 直接调用 Provider（通过服务发现）
     */
    @GetMapping("/call-provider")
    public Map<String, Object> callProvider() {
        // 使用服务名称直接调用服务提供者
        String url = "http://service-provider/api/hello";
        Map<String, Object> providerResponse = restTemplate.getForObject(url, Map.class);

        Map<String, Object> result = new HashMap<>();
        result.put("callType", "direct");
        result.put("consumer", message);
        result.put("consumerPort", port);
        result.put("consumerVersion", version);
        result.put("providerResponse", providerResponse);
        return result;
    }
    
    /**
     * 通过 Gateway 调用 Provider
     */
    @GetMapping("/call-provider-via-gateway")
    public Map<String, Object> callProviderViaGateway() {
        // 通过 Gateway 调用服务提供者
        String url = gatewayUrl + "/provider/hello";
        Map<String, Object> providerResponse = restTemplate.getForObject(url, Map.class);

        Map<String, Object> result = new HashMap<>();
        result.put("callType", "via-gateway");
        result.put("gatewayUrl", gatewayUrl);
        result.put("consumer", message);
        result.put("consumerPort", port);
        result.put("consumerVersion", version);
        result.put("providerResponse", providerResponse);
        return result;
    }
    
    /**
     * 通过 Gateway 调用 Provider 的配置接口
     */
    @GetMapping("/call-provider-config-via-gateway")
    public Map<String, Object> callProviderConfigViaGateway() {
        // 通过 Gateway 调用 Provider 的配置接口
        String url = gatewayUrl + "/provider/config";
        Map<String, Object> providerResponse = restTemplate.getForObject(url, Map.class);

        Map<String, Object> result = new HashMap<>();
        result.put("callType", "via-gateway");
        result.put("gatewayUrl", gatewayUrl);
        result.put("requestedEndpoint", "/provider/config");
        result.put("consumer", message);
        result.put("consumerPort", port);
        result.put("providerResponse", providerResponse);
        return result;
    }
    
    /**
     * 对比两种调用方式
     */
    @GetMapping("/compare-call-methods")
    public Map<String, Object> compareCallMethods() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 直接调用
            long directStartTime = System.currentTimeMillis();
            String directUrl = "http://service-provider/api/hello";
            Map<String, Object> directResponse = restTemplate.getForObject(directUrl, Map.class);
            long directDuration = System.currentTimeMillis() - directStartTime;
            
            // 通过 Gateway 调用
            long gatewayStartTime = System.currentTimeMillis();
            String gatewayUrl = this.gatewayUrl + "/provider/hello";
            Map<String, Object> gatewayResponse = restTemplate.getForObject(gatewayUrl, Map.class);
            long gatewayDuration = System.currentTimeMillis() - gatewayStartTime;
            
            result.put("directCall", Map.of(
                "url", directUrl,
                "duration", directDuration + "ms",
                "response", directResponse
            ));
            
            result.put("gatewayCall", Map.of(
                "url", gatewayUrl,
                "duration", gatewayDuration + "ms",
                "response", gatewayResponse
            ));
            
            result.put("performance", Map.of(
                "directCallFaster", directDuration < gatewayDuration,
                "timeDifference", Math.abs(gatewayDuration - directDuration) + "ms"
            ));
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
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
        
        // 获取gateway-service的实例信息
        List<ServiceInstance> gatewayInstances = discoveryClient.getInstances("gateway-service");
        result.put("gatewayInstances", gatewayInstances);

        return result;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("message", message);
        config.put("version", version);
        config.put("port", port);
        config.put("gatewayUrl", gatewayUrl);
        return config;
    }

}

