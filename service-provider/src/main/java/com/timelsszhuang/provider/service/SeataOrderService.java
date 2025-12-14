package com.timelsszhuang.provider.service;

import com.timelsszhuang.provider.entity.OrderEntity;
import com.timelsszhuang.provider.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 订单服务 (Seata Demo)
 *
 * @author timelsszhuang
 */
@Service
public class SeataOrderService {

    private static final Logger logger = LoggerFactory.getLogger(SeataOrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 创建订单
     *
     * @param userId        用户ID
     * @param commodityCode 商品编码
     * @param count         数量
     * @param amount        金额
     * @return 订单实体
     */
    @Transactional
    public OrderEntity createOrder(String userId, String commodityCode, Integer count, BigDecimal amount) {
        logger.info("==> 开始创建订单: userId={}, commodityCode={}, count={}, amount={}",
                userId, commodityCode, count, amount);

        // 生成订单编号
        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 创建订单
        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCommodityCode(commodityCode);
        order.setCount(count);
        order.setAmount(amount);
        order.setStatus(0); // 创建中

        // 保存订单
        OrderEntity savedOrder = orderRepository.save(order);

        logger.info("==> 订单创建成功: orderNo={}", orderNo);
        return savedOrder;
    }

    /**
     * 更新订单状态为已完成
     */
    @Transactional
    public void completeOrder(String orderNo) {
        logger.info("==> 更新订单状态为已完成: orderNo={}", orderNo);
        
        OrderEntity order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));
        
        order.setStatus(1); // 已完成
        orderRepository.save(order);
        
        logger.info("==> 订单状态更新成功: orderNo={}, status=1", orderNo);
    }

    /**
     * 根据订单编号查询订单
     */
    public OrderEntity getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo).orElse(null);
    }
}
