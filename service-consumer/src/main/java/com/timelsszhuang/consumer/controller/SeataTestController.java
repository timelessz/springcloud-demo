package com.timelsszhuang.consumer.controller;

import com.timelsszhuang.consumer.service.BusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Seata 分布式事务测试控制器
 *
 * @author timelsszhuang
 */
@RestController
@RequestMapping("/api/seata")
public class SeataTestController {

    private static final Logger logger = LoggerFactory.getLogger(SeataTestController.class);

    @Autowired
    private BusinessService businessService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gateway.url:http://localhost:8085}")
    private String gatewayUrl;

    /**
     * 购买商品 - 分布式事务演示
     * 
     * 使用示例：
     * POST /api/seata/purchase
     * {
     *   "userId": "1",
     *   "commodityCode": "PRODUCT-001",
     *   "count": 1,
     *   "amount": 100
     * }
     * 
     * 可选参数：forceError=true 强制触发异常，演示回滚
     */
    @PostMapping("/purchase")
    public Map<String, Object> purchase(@RequestBody PurchaseRequest request,
                                        @RequestParam(defaultValue = "false") boolean forceError) {
        logger.info("收到购买请求: userId={}, commodityCode={}, count={}, amount={}, forceError={}",
                request.getUserId(), request.getCommodityCode(),
                request.getCount(), request.getAmount(), forceError);

        try {
            return businessService.purchase(
                    request.getUserId(),
                    request.getCommodityCode(),
                    request.getCount(),
                    request.getAmount(),
                    forceError
            );
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "购买失败，分布式事务已回滚: " + e.getMessage());
            result.put("data", null);
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }

    /**
     * 查询库存信息
     */
    @GetMapping("/storage/{commodityCode}")
    public Map<String, Object> getStorage(@PathVariable String commodityCode) {
        String url = gatewayUrl + "/provider/storage/" + commodityCode;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "查询库存失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 查询账户信息
     */
    @GetMapping("/account/{userId}")
    public Map<String, Object> getAccount(@PathVariable String userId) {
        String url = gatewayUrl + "/provider/account/" + userId;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "查询账户失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 查询订单信息
     */
    @GetMapping("/order/{orderNo}")
    public Map<String, Object> getOrder(@PathVariable String orderNo) {
        String url = gatewayUrl + "/provider/seata-order/" + orderNo;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "查询订单失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取 Demo 说明和测试用例
     */
    @GetMapping("/demo-info")
    public Map<String, Object> getDemoInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Seata 分布式事务 Demo");
        info.put("description", "演示购买商品的分布式事务：创建订单 -> 扣减库存 -> 扣减余额");

        // 测试用例
        Map<String, Object> testCases = new HashMap<>();

        // 正常购买
        Map<String, Object> normalCase = new HashMap<>();
        normalCase.put("name", "正常购买");
        normalCase.put("url", "POST /api/seata/purchase");
        normalCase.put("body", Map.of(
                "userId", "1",
                "commodityCode", "PRODUCT-001",
                "count", 1,
                "amount", 100
        ));
        normalCase.put("expected", "成功购买，订单创建，库存减少，余额减少");
        testCases.put("case1_normal", normalCase);

        // 库存不足
        Map<String, Object> storageCase = new HashMap<>();
        storageCase.put("name", "库存不足 - 回滚");
        storageCase.put("url", "POST /api/seata/purchase");
        storageCase.put("body", Map.of(
                "userId", "1",
                "commodityCode", "PRODUCT-001",
                "count", 1000,
                "amount", 100
        ));
        storageCase.put("expected", "库存不足异常，事务回滚，订单被删除");
        testCases.put("case2_storage_not_enough", storageCase);

        // 余额不足
        Map<String, Object> accountCase = new HashMap<>();
        accountCase.put("name", "余额不足 - 回滚");
        accountCase.put("url", "POST /api/seata/purchase");
        accountCase.put("body", Map.of(
                "userId", "1",
                "commodityCode", "PRODUCT-001",
                "count", 1,
                "amount", 100000
        ));
        accountCase.put("expected", "余额不足异常，事务回滚，订单和库存都回滚");
        testCases.put("case3_balance_not_enough", accountCase);

        // 强制异常
        Map<String, Object> errorCase = new HashMap<>();
        errorCase.put("name", "强制异常 - 回滚演示");
        errorCase.put("url", "POST /api/seata/purchase?forceError=true");
        errorCase.put("body", Map.of(
                "userId", "1",
                "commodityCode", "PRODUCT-001",
                "count", 1,
                "amount", 100
        ));
        errorCase.put("expected", "业务执行成功但手动抛出异常，所有操作回滚");
        testCases.put("case4_force_error", errorCase);

        info.put("testCases", testCases);

        // 查询接口
        Map<String, String> queryApis = new HashMap<>();
        queryApis.put("查询库存", "GET /api/seata/storage/{commodityCode}");
        queryApis.put("查询账户", "GET /api/seata/account/{userId}");
        queryApis.put("查询订单", "GET /api/seata/order/{orderNo}");
        info.put("queryApis", queryApis);

        // 初始数据
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("storage", "PRODUCT-001: 100件, PRODUCT-002: 200件");
        initialData.put("account", "用户1: 10000元, 用户2: 5000元");
        info.put("initialData", initialData);

        return info;
    }

    /**
     * 购买请求
     */
    public static class PurchaseRequest {
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
