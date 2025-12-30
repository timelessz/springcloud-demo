package com.timelsszhuang.consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单服务调用类
 * 通过 Gateway 调用 Provider 的订单服务
 *
 * @author timelsszhuang
 */
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    @Qualifier("plainRestTemplate")
    private RestTemplate restTemplate;

    @Value("${gateway.url:http://localhost:8085}")
    private String gatewayUrl;

    /**
     * 创建订单
     */
    public Map<String, Object> createOrder(String token, String username, String productName,
                                          Integer quantity, BigDecimal price, String remark) {
        String url = gatewayUrl + "/provider/order/create";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("productName", productName);
        requestBody.put("quantity", quantity);
        requestBody.put("price", price);
        requestBody.put("remark", remark);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            logger.info("创建订单成功: username={}, product={}", username, productName);
            return response.getBody();
        } catch (Exception e) {
            logger.error("创建订单失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 获取订单详情
     */
    public Map<String, Object> getOrder(String token, String orderId) {
        String url = gatewayUrl + "/provider/order/" + orderId;

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("查询订单成功: orderId={}", orderId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("查询订单失败: orderId={}", orderId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 获取我的订单列表
     */
    public Map<String, Object> getMyOrders(String token) {
        String url = gatewayUrl + "/provider/order/my-orders";

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("查询我的订单成功");
            return response.getBody();
        } catch (Exception e) {
            logger.error("查询我的订单失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 获取指定用户的订单列表
     */
    public Map<String, Object> getUserOrders(String token, String username) {
        String url = gatewayUrl + "/provider/order/user/" + username;

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("查询用户订单成功: username={}", username);
            return response.getBody();
        } catch (Exception e) {
            logger.error("查询用户订单失败: username={}", username, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 获取所有订单
     */
    public Map<String, Object> getAllOrders(String token) {
        String url = gatewayUrl + "/provider/order/list";

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("查询所有订单成功");
            return response.getBody();
        } catch (Exception e) {
            logger.error("查询所有订单失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 更新订单状态
     */
    public Map<String, Object> updateOrderStatus(String token, String orderId, String status) {
        String url = gatewayUrl + "/provider/order/" + orderId + "/status";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("status", status);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            logger.info("更新订单状态成功: orderId={}, status={}", orderId, status);
            return response.getBody();
        } catch (Exception e) {
            logger.error("更新订单状态失败: orderId={}", orderId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 取消订单
     */
    public Map<String, Object> cancelOrder(String token, String orderId) {
        String url = gatewayUrl + "/provider/order/" + orderId + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            logger.info("取消订单成功: orderId={}", orderId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("取消订单失败: orderId={}", orderId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 删除订单
     */
    public Map<String, Object> deleteOrder(String token, String orderId) {
        String url = gatewayUrl + "/provider/order/" + orderId;

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);
            logger.info("删除订单成功: orderId={}", orderId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("删除订单失败: orderId={}", orderId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 获取订单统计信息
     */
    public Map<String, Object> getStatistics(String token) {
        String url = gatewayUrl + "/provider/order/statistics";

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            logger.info("查询订单统计成功");
            return response.getBody();
        } catch (Exception e) {
            logger.error("查询订单统计失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }

    /**
     * 初始化测试数据
     */
    public Map<String, Object> initTestData(String token) {
        String url = gatewayUrl + "/provider/order/init-test-data";

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            logger.info("初始化测试数据成功");
            return response.getBody();
        } catch (Exception e) {
            logger.error("初始化测试数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "调用订单服务失败: " + e.getMessage());
            errorResponse.put("data", null);
            return errorResponse;
        }
    }
}

