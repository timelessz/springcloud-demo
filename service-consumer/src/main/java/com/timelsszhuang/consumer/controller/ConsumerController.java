package com.timelsszhuang.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RefreshScope  // 支持Nacos配置动态刷新
public class ConsumerController {

    @Autowired
    @Qualifier("loadBalancedRestTemplate")
    private RestTemplate loadBalancedRestTemplate;  // 用于服务名调用

    @Autowired
    @Qualifier("plainRestTemplate")
    private RestTemplate plainRestTemplate;  // 用于直接调用 IP:PORT

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${server.port}")
    private String port;

    @Value("${consumer.message:Hello from Consumer}")
    private String message;

    @Value("${consumer.version:1.0}")
    private String version;
    
    @Value("${gateway.url:http://127.0.0.1:8085}")
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
    public Map<String, Object> callProvider(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        String url = "http://service-provider/api/hello";
        try {
            // 构建带 Token 的请求头
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", token);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 使用 exchange 获取完整响应（状态码 + body）
            ResponseEntity<Map> response = loadBalancedRestTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            result.put("statusCode", response.getStatusCode().value());
            result.put("statusText", ((HttpStatus) response.getStatusCode()).getReasonPhrase());
            result.put("callType", "direct");
            result.put("consumer", message);
            result.put("consumerPort", port);
            result.put("consumerVersion", version);
            result.put("providerResponse", response.getBody());
        } catch (HttpClientErrorException e) {
            // 4xx 错误
            result.put("statusCode", e.getStatusCode().value());
            result.put("statusText", e.getStatusText());
            result.put("error", "客户端错误: " + e.getMessage());
            result.put("responseBody", e.getResponseBodyAsString());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (HttpServerErrorException e) {
            // 5xx 错误
            result.put("statusCode", e.getStatusCode().value());
            result.put("statusText", e.getStatusText());
            result.put("error", "服务端错误: " + e.getMessage());
            result.put("responseBody", e.getResponseBodyAsString());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (ResourceAccessException e) {
            // 连接错误
            result.put("statusCode", -1);
            result.put("error", "连接失败: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (RestClientException e) {
            result.put("statusCode", -1);
            result.put("error", "调用 Provider 失败: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        result.put("url", url);
        return result;
    }
    
    /**
     * 通过 Gateway 调用 Provider
     */
    @GetMapping("/call-provider-via-gateway")
    public Map<String, Object> callProviderViaGateway(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        String url = gatewayUrl + "/provider/hello";
        try {
            // 构建带 Token 的请求头
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", token);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 使用 exchange 获取完整响应（状态码 + body）
            ResponseEntity<Map> response = plainRestTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            result.put("statusCode", response.getStatusCode().value());
            result.put("statusText", ((HttpStatus) response.getStatusCode()).getReasonPhrase());
            result.put("callType", "via-gateway");
            result.put("gatewayUrl", gatewayUrl);
            result.put("consumer", message);
            result.put("consumerPort", port);
            result.put("consumerVersion", version);
            result.put("providerResponse", response.getBody());
        } catch (HttpClientErrorException e) {
            // 4xx 错误
            result.put("statusCode", e.getStatusCode().value());
            result.put("statusText", e.getStatusText());
            result.put("error", "客户端错误: " + e.getMessage());
            result.put("responseBody", e.getResponseBodyAsString());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (HttpServerErrorException e) {
            // 5xx 错误
            result.put("statusCode", e.getStatusCode().value());
            result.put("statusText", e.getStatusText());
            result.put("error", "服务端错误: " + e.getMessage());
            result.put("responseBody", e.getResponseBodyAsString());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (ResourceAccessException e) {
            // 连接错误
            result.put("statusCode", -1);
            result.put("error", "连接失败: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (RestClientException e) {
            result.put("statusCode", -1);
            result.put("error", "通过 Gateway 调用 Provider 失败: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        result.put("url", url);
        result.put("gatewayUrl", gatewayUrl);
        return result;
    }
    
    /**
     * 通过 Gateway 调用 Provider 的配置接口
     */
    @GetMapping("/call-provider-config-via-gateway")
    public Map<String, Object> callProviderConfigViaGateway(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        String url = gatewayUrl + "/provider/config";
        try {
            // 构建带 Token 的请求头
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", token);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 使用 exchange 获取完整响应（状态码 + body）
            ResponseEntity<Map> response = plainRestTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);
            result.put("statusCode", response.getStatusCode().value());
            result.put("statusText", ((HttpStatus) response.getStatusCode()).getReasonPhrase());
            result.put("callType", "via-gateway");
            result.put("gatewayUrl", gatewayUrl);
            result.put("requestedEndpoint", "/provider/config");
            result.put("consumer", message);
            result.put("consumerPort", port);
            result.put("providerResponse", response.getBody());
        } catch (HttpClientErrorException e) {
            // 4xx 错误
            result.put("statusCode", e.getStatusCode().value());
            result.put("statusText", e.getStatusText());
            result.put("error", "客户端错误: " + e.getMessage());
            result.put("responseBody", e.getResponseBodyAsString());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (HttpServerErrorException e) {
            // 5xx 错误
            result.put("statusCode", e.getStatusCode().value());
            result.put("statusText", e.getStatusText());
            result.put("error", "服务端错误: " + e.getMessage());
            result.put("responseBody", e.getResponseBodyAsString());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (ResourceAccessException e) {
            // 连接错误
            result.put("statusCode", -1);
            result.put("error", "连接失败: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        } catch (RestClientException e) {
            result.put("statusCode", -1);
            result.put("error", "通过 Gateway 调用 Provider 配置接口失败: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        result.put("url", url);
        result.put("gatewayUrl", gatewayUrl);
        return result;
    }
    
    /**
     * 对比两种调用方式
     */
    @GetMapping("/compare-call-methods")
    public Map<String, Object> compareCallMethods(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建带 Token 的请求头
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 直接调用
        Map<String, Object> directCallResult = new HashMap<>();
        String directUrl = "http://service-provider/api/hello";
        try {
            long directStartTime = System.currentTimeMillis();
            ResponseEntity<Map> directResponse = loadBalancedRestTemplate.exchange(
                    directUrl, HttpMethod.GET, entity, Map.class);
            long directDuration = System.currentTimeMillis() - directStartTime;
            
            directCallResult.put("url", directUrl);
            directCallResult.put("statusCode", directResponse.getStatusCode().value());
            directCallResult.put("statusText", ((HttpStatus) directResponse.getStatusCode()).getReasonPhrase());
            directCallResult.put("duration", directDuration + "ms");
            directCallResult.put("response", directResponse.getBody());
            directCallResult.put("success", true);
        } catch (HttpClientErrorException e) {
            directCallResult.put("success", false);
            directCallResult.put("statusCode", e.getStatusCode().value());
            directCallResult.put("statusText", e.getStatusText());
            directCallResult.put("error", e.getMessage());
            directCallResult.put("responseBody", e.getResponseBodyAsString());
            directCallResult.put("errorType", e.getClass().getSimpleName());
        } catch (HttpServerErrorException e) {
            directCallResult.put("success", false);
            directCallResult.put("statusCode", e.getStatusCode().value());
            directCallResult.put("statusText", e.getStatusText());
            directCallResult.put("error", e.getMessage());
            directCallResult.put("responseBody", e.getResponseBodyAsString());
            directCallResult.put("errorType", e.getClass().getSimpleName());
        } catch (ResourceAccessException e) {
            directCallResult.put("success", false);
            directCallResult.put("statusCode", -1);
            directCallResult.put("error", e.getMessage());
            directCallResult.put("errorType", e.getClass().getSimpleName());
        } catch (RestClientException e) {
            directCallResult.put("success", false);
            directCallResult.put("statusCode", -1);
            directCallResult.put("error", e.getMessage());
            directCallResult.put("errorType", e.getClass().getSimpleName());
        }
        directCallResult.put("url", directUrl);
        result.put("directCall", directCallResult);

        // 通过 Gateway 调用
        Map<String, Object> gatewayCallResult = new HashMap<>();
        String gatewayCallUrl = this.gatewayUrl + "/provider/hello";
        try {
            long gatewayStartTime = System.currentTimeMillis();
            ResponseEntity<Map> gatewayResponse = plainRestTemplate.exchange(
                    gatewayCallUrl, HttpMethod.GET, entity, Map.class);
            long gatewayDuration = System.currentTimeMillis() - gatewayStartTime;
            
            gatewayCallResult.put("url", gatewayCallUrl);
            gatewayCallResult.put("statusCode", gatewayResponse.getStatusCode().value());
            gatewayCallResult.put("statusText", ((HttpStatus) gatewayResponse.getStatusCode()).getReasonPhrase());
            gatewayCallResult.put("duration", gatewayDuration + "ms");
            gatewayCallResult.put("response", gatewayResponse.getBody());
            gatewayCallResult.put("success", true);
        } catch (HttpClientErrorException e) {
            gatewayCallResult.put("success", false);
            gatewayCallResult.put("statusCode", e.getStatusCode().value());
            gatewayCallResult.put("statusText", e.getStatusText());
            gatewayCallResult.put("error", e.getMessage());
            gatewayCallResult.put("responseBody", e.getResponseBodyAsString());
            gatewayCallResult.put("errorType", e.getClass().getSimpleName());
        } catch (HttpServerErrorException e) {
            gatewayCallResult.put("success", false);
            gatewayCallResult.put("statusCode", e.getStatusCode().value());
            gatewayCallResult.put("statusText", e.getStatusText());
            gatewayCallResult.put("error", e.getMessage());
            gatewayCallResult.put("responseBody", e.getResponseBodyAsString());
            gatewayCallResult.put("errorType", e.getClass().getSimpleName());
        } catch (ResourceAccessException e) {
            gatewayCallResult.put("success", false);
            gatewayCallResult.put("statusCode", -1);
            gatewayCallResult.put("error", e.getMessage());
            gatewayCallResult.put("errorType", e.getClass().getSimpleName());
        } catch (RestClientException e) {
            gatewayCallResult.put("success", false);
            gatewayCallResult.put("statusCode", -1);
            gatewayCallResult.put("error", e.getMessage());
            gatewayCallResult.put("errorType", e.getClass().getSimpleName());
        }
        gatewayCallResult.put("url", gatewayCallUrl);
        result.put("gatewayCall", gatewayCallResult);

        // 性能对比（仅当两者都成功时）
        if (Boolean.TRUE.equals(directCallResult.get("success"))
                && Boolean.TRUE.equals(gatewayCallResult.get("success"))) {
            String directDurationStr = (String) directCallResult.get("duration");
            String gatewayDurationStr = (String) gatewayCallResult.get("duration");
            long directDuration = Long.parseLong(directDurationStr.replace("ms", ""));
            long gatewayDuration = Long.parseLong(gatewayDurationStr.replace("ms", ""));

            result.put("performance", Map.of(
                "directCallFaster", directDuration < gatewayDuration,
                "timeDifference", Math.abs(gatewayDuration - directDuration) + "ms"
            ));
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

