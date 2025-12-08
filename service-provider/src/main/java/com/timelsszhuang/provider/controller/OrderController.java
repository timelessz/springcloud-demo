package com.timelsszhuang.provider.controller;

import com.timelsszhuang.provider.entity.Order;
import com.timelsszhuang.provider.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * 提供订单的增删改查功能
 *
 * @author timelsszhuang
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request,
                                          @RequestHeader(value = "X-User-Name", required = false) String currentUser) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 如果有当前用户信息，优先使用当前用户
            String username = currentUser != null ? currentUser : request.getUsername();

            if (username == null || username.isEmpty()) {
                response.put("code", 400);
                response.put("message", "用户名不能为空");
                response.put("data", null);
                return response;
            }

            // 参数验证
            if (request.getProductName() == null || request.getProductName().isEmpty()) {
                response.put("code", 400);
                response.put("message", "商品名称不能为空");
                response.put("data", null);
                return response;
            }

            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                response.put("code", 400);
                response.put("message", "商品数量必须大于0");
                response.put("data", null);
                return response;
            }

            if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                response.put("code", 400);
                response.put("message", "商品价格必须大于0");
                response.put("data", null);
                return response;
            }

            // 创建订单
            Order order = orderService.createOrder(
                    username,
                    request.getProductName(),
                    request.getQuantity(),
                    request.getPrice(),
                    request.getRemark()
            );

            response.put("code", 200);
            response.put("message", "订单创建成功");
            response.put("data", order);

            logger.info("订单创建成功: orderId={}, username={}", order.getOrderId(), username);

        } catch (Exception e) {
            logger.error("创建订单失败", e);
            response.put("code", 500);
            response.put("message", "订单创建失败: " + e.getMessage());
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 根据订单ID获取订单
     */
    @GetMapping("/{orderId}")
    public Map<String, Object> getOrder(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();

        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", order);
        } else {
            response.put("code", 404);
            response.put("message", "订单不存在");
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 获取当前用户的订单列表
     */
    @GetMapping("/my-orders")
    public Map<String, Object> getMyOrders(@RequestHeader(value = "X-User-Name", required = false) String username) {
        Map<String, Object> response = new HashMap<>();

        if (username == null || username.isEmpty()) {
            response.put("code", 401);
            response.put("message", "未登录或未认证");
            response.put("data", null);
            response.put("timestamp", System.currentTimeMillis());
            return response;
        }

        List<Order> orders = orderService.getOrdersByUsername(username);

        response.put("code", 200);
        response.put("message", "查询成功");

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("orders", orders);
        data.put("count", orders.size());

        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    /**
     * 根据用户名获取订单列表
     */
    @GetMapping("/user/{username}")
    public Map<String, Object> getUserOrders(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();

        List<Order> orders = orderService.getOrdersByUsername(username);

        response.put("code", 200);
        response.put("message", "查询成功");

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("orders", orders);
        data.put("count", orders.size());

        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    /**
     * 获取所有订单
     */
    @GetMapping("/list")
    public Map<String, Object> getAllOrders() {
        Map<String, Object> response = new HashMap<>();

        List<Order> orders = orderService.getAllOrders();

        response.put("code", 200);
        response.put("message", "查询成功");

        Map<String, Object> data = new HashMap<>();
        data.put("orders", orders);
        data.put("count", orders.size());

        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable String orderId,
                                                 @RequestBody UpdateStatusRequest request) {
        Map<String, Object> response = new HashMap<>();

        Order order = orderService.updateOrderStatus(orderId, request.getStatus());

        if (order != null) {
            response.put("code", 200);
            response.put("message", "订单状态更新成功");
            response.put("data", order);
        } else {
            response.put("code", 404);
            response.put("message", "订单不存在");
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();

        boolean success = orderService.cancelOrder(orderId);

        if (success) {
            response.put("code", 200);
            response.put("message", "订单取消成功");
            response.put("data", Map.of("orderId", orderId, "status", "CANCELLED"));
        } else {
            response.put("code", 400);
            response.put("message", "订单取消失败");
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderId}")
    public Map<String, Object> deleteOrder(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();

        boolean success = orderService.deleteOrder(orderId);

        if (success) {
            response.put("code", 200);
            response.put("message", "订单删除成功");
            response.put("data", Map.of("orderId", orderId));
        } else {
            response.put("code", 404);
            response.put("message", "订单不存在");
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> statistics = orderService.getOrderStatistics();

        response.put("code", 200);
        response.put("message", "查询成功");
        response.put("data", statistics);
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    /**
     * 初始化测试数据
     */
    @PostMapping("/init-test-data")
    public Map<String, Object> initTestData() {
        Map<String, Object> response = new HashMap<>();

        try {
            orderService.initTestData();
            response.put("code", 200);
            response.put("message", "测试数据初始化成功");
            response.put("data", null);
        } catch (Exception e) {
            logger.error("初始化测试数据失败", e);
            response.put("code", 500);
            response.put("message", "初始化失败: " + e.getMessage());
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 创建订单请求对象
     */
    public static class CreateOrderRequest {
        private String username;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private String remark;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * 更新状态请求对象
     */
    public static class UpdateStatusRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

