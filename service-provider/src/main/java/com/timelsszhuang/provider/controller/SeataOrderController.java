package com.timelsszhuang.provider.controller;

import com.timelsszhuang.provider.entity.OrderEntity;
import com.timelsszhuang.provider.service.SeataOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Seata 订单控制器 (Seata Demo)
 *
 * @author timelsszhuang
 */
@RestController
@RequestMapping("/api/seata-order")
public class SeataOrderController {

    private static final Logger logger = LoggerFactory.getLogger(SeataOrderController.class);

    @Autowired
    private SeataOrderService seataOrderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("收到创建订单请求: userId={}, commodityCode={}, count={}, amount={}",
                    request.getUserId(), request.getCommodityCode(), 
                    request.getCount(), request.getAmount());

            OrderEntity order = seataOrderService.createOrder(
                    request.getUserId(),
                    request.getCommodityCode(),
                    request.getCount(),
                    request.getAmount()
            );

            response.put("code", 200);
            response.put("message", "订单创建成功");
            response.put("data", order);
        } catch (Exception e) {
            logger.error("创建订单失败", e);
            response.put("code", 500);
            response.put("message", e.getMessage());
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 更新订单状态为已完成
     */
    @PostMapping("/{orderNo}/complete")
    public Map<String, Object> completeOrder(@PathVariable String orderNo) {
        Map<String, Object> response = new HashMap<>();

        try {
            seataOrderService.completeOrder(orderNo);

            response.put("code", 200);
            response.put("message", "订单状态更新成功");
            response.put("data", null);
        } catch (Exception e) {
            logger.error("更新订单状态失败", e);
            response.put("code", 500);
            response.put("message", e.getMessage());
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 查询订单
     */
    @GetMapping("/{orderNo}")
    public Map<String, Object> getOrder(@PathVariable String orderNo) {
        Map<String, Object> response = new HashMap<>();

        OrderEntity order = seataOrderService.getOrderByOrderNo(orderNo);

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
     * 创建订单请求
     */
    public static class CreateOrderRequest {
        private String userId;
        private String commodityCode;
        private Integer count;
        private BigDecimal amount;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCommodityCode() {
            return commodityCode;
        }

        public void setCommodityCode(String commodityCode) {
            this.commodityCode = commodityCode;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
