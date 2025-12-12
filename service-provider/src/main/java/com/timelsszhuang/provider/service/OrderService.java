package com.timelsszhuang.provider.service;

import com.timelsszhuang.provider.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 订单服务类
 * 使用内存存储模拟数据库操作
 *
 * @author timelsszhuang
 */
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /**
     * 使用 ConcurrentHashMap 模拟数据库存储
     */
    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();

    /**
     * 订单ID计数器
     */
    private long orderIdCounter = 1000;

    /**
     * 创建订单
     */
    public Order createOrder(String username, String productName, Integer quantity, BigDecimal price, String remark) {
        // 生成订单ID
        String orderId = generateOrderId();

        // 创建订单
        Order order = new Order(orderId, username, productName, quantity, price);
        order.setRemark(remark);

        // 存储订单
        orderStore.put(orderId, order);

        logger.info("创建订单成功: orderId={}, username={}, product={}, quantity={}, totalAmount={}",
                orderId, username, productName, quantity, order.getTotalAmount());

        return order;
    }

    /**
     * 根据订单ID获取订单
     */
    public Order getOrderById(String orderId) {
        Order order = orderStore.get(orderId);
        if (order != null) {
            logger.info("查询订单: orderId={}, status={}", orderId, order.getStatus());
        } else {
            logger.warn("订单不存在: orderId={}", orderId);
        }
        return order;
    }

    /**
     * 根据用户名获取订单列表
     */
    public List<Order> getOrdersByUsername(String username) {
        List<Order> orders = orderStore.values().stream()
                .filter(order -> order.getUsername().equals(username))
                .sorted(Comparator.comparing(Order::getCreateTime).reversed())
                .collect(Collectors.toList());

        logger.info("查询用户订单: username={}, count={}", username, orders.size());
        return orders;
    }

    /**
     * 获取所有订单
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>(orderStore.values());
        orders.sort(Comparator.comparing(Order::getCreateTime).reversed());
        logger.info("查询所有订单: count={}", orders.size());
        return orders;
    }

    /**
     * 更新订单状态
     */
    public Order updateOrderStatus(String orderId, String status) {
        Order order = orderStore.get(orderId);
        if (order == null) {
            logger.warn("更新订单状态失败，订单不存在: orderId={}", orderId);
            return null;
        }

        String oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());

        logger.info("更新订单状态: orderId={}, {} -> {}", orderId, oldStatus, status);
        return order;
    }

    /**
     * 取消订单
     */
    public boolean cancelOrder(String orderId) {
        Order order = orderStore.get(orderId);
        if (order == null) {
            logger.warn("取消订单失败，订单不存在: orderId={}", orderId);
            return false;
        }

        if ("CANCELLED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
            logger.warn("订单无法取消，当前状态: orderId={}, status={}", orderId, order.getStatus());
            return false;
        }

        order.setStatus("CANCELLED");
        order.setUpdateTime(LocalDateTime.now());

        logger.info("取消订单成功: orderId={}", orderId);
        return true;
    }

    /**
     * 删除订单
     */
    public boolean deleteOrder(String orderId) {
        Order removed = orderStore.remove(orderId);
        if (removed != null) {
            logger.info("删除订单成功: orderId={}", orderId);
            return true;
        } else {
            logger.warn("删除订单失败，订单不存在: orderId={}", orderId);
            return false;
        }
    }

    /**
     * 统计订单信息
     */
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalOrders = orderStore.size();
        BigDecimal totalAmount = orderStore.values().stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> statusCount = orderStore.values().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        stats.put("totalOrders", totalOrders);
        stats.put("totalAmount", totalAmount);
        stats.put("statusCount", statusCount);

        logger.info("订单统计: totalOrders={}, totalAmount={}", totalOrders, totalAmount);
        return stats;
    }

    /**
     * 生成订单ID
     */
    private synchronized String generateOrderId() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", orderIdCounter++);
    }

    /**
     * 初始化一些测试数据
     */
    public void initTestData() {
        createOrder("admin", "MacBook Pro", 1, new BigDecimal("12999.00"), "测试订单1");
        createOrder("admin", "iPhone 15", 2, new BigDecimal("5999.00"), "测试订单2");
        createOrder("user", "iPad Air", 1, new BigDecimal("4799.00"), "测试订单3");
        createOrder("test", "AirPods Pro", 3, new BigDecimal("1999.00"), "测试订单4");

        logger.info("初始化测试订单数据完成");
    }

}

