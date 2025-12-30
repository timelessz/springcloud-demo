package com.timelsszhuang.consumer.service;

import io.seata.spring.annotation.GlobalTransactional;
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
 * 业务聚合服务 (Seata Demo)
 * 使用 @GlobalTransactional 实现分布式事务
 *
 * @author timelsszhuang
 */
@Service
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);

    @Autowired
    @Qualifier("plainRestTemplate")
    private RestTemplate restTemplate;

    @Value("${gateway.url:http://localhost:8085}")
    private String gatewayUrl;

    /**
     * 购买商品 - 分布式事务
     * 
     * 业务流程：
     * 1. 创建订单
     * 2. 扣减库存
     * 3. 扣减账户余额
     * 
     * 使用 @GlobalTransactional 确保三个操作在同一个分布式事务中
     * 任何一步失败都会触发全局回滚
     *
     * @param userId        用户ID
     * @param commodityCode 商品编码
     * @param count         购买数量
     * @param amount        购买金额
     * @param forceError    是否强制抛出异常（用于演示回滚）
     * @return 业务执行结果
     */
    @GlobalTransactional(name = "purchase-transaction", rollbackFor = Exception.class)
    public Map<String, Object> purchase(String userId, String commodityCode, 
                                         Integer count, BigDecimal amount, 
                                         boolean forceError) {
        
        logger.info("========== 开始分布式事务: userId={}, commodityCode={}, count={}, amount={} ==========",
                userId, commodityCode, count, amount);

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> steps = new HashMap<>();

        try {
            // 步骤1: 创建订单
            logger.info(">>> 步骤1: 创建订单");
            Map<String, Object> orderResult = createOrder(userId, commodityCode, count, amount);
            steps.put("step1_order", orderResult);
            
            if (!isSuccess(orderResult)) {
                throw new RuntimeException("创建订单失败: " + orderResult.get("message"));
            }
            
            String orderNo = extractOrderNo(orderResult);
            logger.info(">>> 订单创建成功: orderNo={}", orderNo);

            // 步骤2: 扣减库存
            logger.info(">>> 步骤2: 扣减库存");
            Map<String, Object> storageResult = deductStorage(commodityCode, count);
            steps.put("step2_storage", storageResult);
            
            if (!isSuccess(storageResult)) {
                throw new RuntimeException("扣减库存失败: " + storageResult.get("message"));
            }
            logger.info(">>> 库存扣减成功");

            // 步骤3: 扣减账户余额
            logger.info(">>> 步骤3: 扣减余额");
            Map<String, Object> accountResult = deductBalance(userId, amount);
            steps.put("step3_account", accountResult);
            
            if (!isSuccess(accountResult)) {
                throw new RuntimeException("扣减余额失败: " + accountResult.get("message"));
            }
            logger.info(">>> 余额扣减成功");

            // 步骤4: 模拟异常（用于演示回滚）
            if (forceError) {
                logger.warn(">>> 强制抛出异常，演示分布式事务回滚");
                throw new RuntimeException("模拟业务异常，触发分布式事务回滚!");
            }

            // 步骤5: 更新订单状态为已完成
            logger.info(">>> 步骤5: 更新订单状态");
            Map<String, Object> completeResult = completeOrder(orderNo);
            steps.put("step4_complete", completeResult);

            result.put("code", 200);
            result.put("message", "购买成功");
            result.put("orderNo", orderNo);

            logger.info("========== 分布式事务成功完成 ==========");

        } catch (Exception e) {
            logger.error("========== 分布式事务失败，将回滚 ==========", e);
            result.put("code", 500);
            result.put("message", "购买失败: " + e.getMessage());
            throw e; // 重新抛出异常触发 Seata 回滚
        }

        result.put("data", steps);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 创建订单
     */
    private Map<String, Object> createOrder(String userId, String commodityCode, 
                                            Integer count, BigDecimal amount) {
        String url = gatewayUrl + "/provider/seata-order/create";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", userId);
        requestBody.put("commodityCode", commodityCode);
        requestBody.put("count", count);
        requestBody.put("amount", amount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("调用订单服务失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("message", "调用订单服务失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 扣减库存
     */
    private Map<String, Object> deductStorage(String commodityCode, Integer count) {
        String url = gatewayUrl + "/provider/storage/deduct";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("commodityCode", commodityCode);
        requestBody.put("count", count);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("调用库存服务失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("message", "调用库存服务失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 扣减账户余额
     */
    private Map<String, Object> deductBalance(String userId, BigDecimal amount) {
        String url = gatewayUrl + "/provider/account/deduct";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", userId);
        requestBody.put("amount", amount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("调用账户服务失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("message", "调用账户服务失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 更新订单状态为已完成
     */
    private Map<String, Object> completeOrder(String orderNo) {
        String url = gatewayUrl + "/provider/seata-order/" + orderNo + "/complete";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("更新订单状态失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("message", "更新订单状态失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 判断服务调用是否成功
     */
    private boolean isSuccess(Map<String, Object> result) {
        if (result == null) return false;
        Object code = result.get("code");
        return code != null && (code.equals(200) || code.equals("200"));
    }

    /**
     * 从订单结果中提取订单编号
     */
    @SuppressWarnings("unchecked")
    private String extractOrderNo(Map<String, Object> orderResult) {
        Object data = orderResult.get("data");
        if (data instanceof Map) {
            Map<String, Object> orderData = (Map<String, Object>) data;
            return (String) orderData.get("orderNo");
        }
        return null;
    }
}
