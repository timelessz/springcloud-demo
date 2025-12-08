package com.timelsszhuang.consumer.controller;

import com.timelsszhuang.consumer.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单测试控制器
 * 测试通过 Gateway 调用 Provider 的订单服务
 *
 * @author timelsszhuang
 */
@RestController
@RequestMapping("/api/order-test")
public class OrderTestController {

    private static final Logger logger = LoggerFactory.getLogger(OrderTestController.class);

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单测试
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request,
                                          @RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试创建订单: username={}, product={}", request.getUsername(), request.getProductName());

        return orderService.createOrder(
                token,
                request.getUsername(),
                request.getProductName(),
                request.getQuantity(),
                request.getPrice(),
                request.getRemark()
        );
    }

    /**
     * 获取订单详情测试
     */
    @GetMapping("/{orderId}")
    public Map<String, Object> getOrder(@PathVariable String orderId,
                                       @RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试查询订单: orderId={}", orderId);
        return orderService.getOrder(token, orderId);
    }

    /**
     * 获取我的订单列表测试
     */
    @GetMapping("/my-orders")
    public Map<String, Object> getMyOrders(@RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试查询我的订单");
        return orderService.getMyOrders(token);
    }

    /**
     * 获取指定用户的订单列表测试
     */
    @GetMapping("/user/{username}")
    public Map<String, Object> getUserOrders(@PathVariable String username,
                                            @RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试查询用户订单: username={}", username);
        return orderService.getUserOrders(token, username);
    }

    /**
     * 获取所有订单测试
     */
    @GetMapping("/list")
    public Map<String, Object> getAllOrders(@RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试查询所有订单");
        return orderService.getAllOrders(token);
    }

    /**
     * 更新订单状态测试
     */
    @PutMapping("/{orderId}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable String orderId,
                                                 @RequestBody UpdateStatusRequest request,
                                                 @RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试更新订单状态: orderId={}, status={}", orderId, request.getStatus());
        return orderService.updateOrderStatus(token, orderId, request.getStatus());
    }

    /**
     * 取消订单测试
     */
    @PostMapping("/{orderId}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable String orderId,
                                          @RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试取消订单: orderId={}", orderId);
        return orderService.cancelOrder(token, orderId);
    }

    /**
     * 删除订单测试
     */
    @DeleteMapping("/{orderId}")
    public Map<String, Object> deleteOrder(@PathVariable String orderId,
                                          @RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试删除订单: orderId={}", orderId);
        return orderService.deleteOrder(token, orderId);
    }

    /**
     * 获取订单统计信息测试
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(@RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试查询订单统计");
        return orderService.getStatistics(token);
    }

    /**
     * 初始化测试数据
     */
    @PostMapping("/init-test-data")
    public Map<String, Object> initTestData(@RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("测试初始化数据");
        return orderService.initTestData(token);
    }

    /**
     * 完整流程测试
     * 测试创建订单、查询订单、更新状态、取消订单的完整流程
     */
    @PostMapping("/full-flow-test")
    public Map<String, Object> fullFlowTest(@RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("开始完整流程测试");

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> steps = new HashMap<>();

        try {
            // 步骤1: 创建订单
            logger.info("步骤1: 创建订单");
            Map<String, Object> createResult = orderService.createOrder(
                    token,
                    "testuser",
                    "Test Product",
                    2,
                    new BigDecimal("99.99"),
                    "完整流程测试订单"
            );
            steps.put("step1_create", createResult);

            // 获取订单ID
            String orderId = null;
            if (createResult != null && createResult.get("data") != null) {
                Map<String, Object> orderData = (Map<String, Object>) createResult.get("data");
                orderId = (String) orderData.get("orderId");
            }

            if (orderId != null) {
                // 步骤2: 查询订单
                logger.info("步骤2: 查询订单");
                Map<String, Object> getResult = orderService.getOrder(token, orderId);
                steps.put("step2_get", getResult);

                // 步骤3: 更新订单状态为已支付
                logger.info("步骤3: 更新订单状态为已支付");
                Map<String, Object> updateResult1 = orderService.updateOrderStatus(token, orderId, "PAID");
                steps.put("step3_update_to_paid", updateResult1);

                // 步骤4: 更新订单状态为已发货
                logger.info("步骤4: 更新订单状态为已发货");
                Map<String, Object> updateResult2 = orderService.updateOrderStatus(token, orderId, "SHIPPED");
                steps.put("step4_update_to_shipped", updateResult2);

                // 步骤5: 查询订单列表
                logger.info("步骤5: 查询订单列表");
                Map<String, Object> listResult = orderService.getAllOrders(token);
                steps.put("step5_list", listResult);

                result.put("code", 200);
                result.put("message", "完整流程测试成功");
                result.put("orderId", orderId);
            } else {
                result.put("code", 500);
                result.put("message", "创建订单失败，无法继续测试");
            }

        } catch (Exception e) {
            logger.error("完整流程测试失败", e);
            result.put("code", 500);
            result.put("message", "测试失败: " + e.getMessage());
        }

        result.put("data", steps);
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 并发测试
     * 快速创建多个订单，测试系统性能和 Sentinel 限流
     */
    @PostMapping("/concurrent-test")
    public Map<String, Object> concurrentTest(@RequestParam(defaultValue = "5") int count,
                                             @RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("开始并发测试: count={}", count);

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (int i = 1; i <= count; i++) {
            try {
                Map<String, Object> createResult = orderService.createOrder(
                        token,
                        "testuser",
                        "Concurrent Test Product " + i,
                        1,
                        new BigDecimal("9.99"),
                        "并发测试订单 " + i
                );

                Integer code = (Integer) createResult.get("code");
                if (code != null && code == 200) {
                    successCount++;
                    logger.info("订单创建成功: {}/{}", i, count);
                } else {
                    failCount++;
                    logger.warn("订单创建失败: {}/{}, reason={}", i, count, createResult.get("message"));
                }

            } catch (Exception e) {
                failCount++;
                logger.error("订单创建异常: {}/{}", i, count, e);
            }
        }

        result.put("code", 200);
        result.put("message", "并发测试完成");

        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", count);
        data.put("successCount", successCount);
        data.put("failCount", failCount);
        data.put("successRate", String.format("%.2f%%", (successCount * 100.0 / count)));

        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());

        return result;
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

