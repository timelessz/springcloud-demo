package com.timelsszhuang.provider.controller;

import com.timelsszhuang.provider.entity.StorageEntity;
import com.timelsszhuang.provider.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 库存控制器 (Seata Demo)
 *
 * @author timelsszhuang
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    @Autowired
    private StorageService storageService;

    /**
     * 扣减库存
     */
    @PostMapping("/deduct")
    public Map<String, Object> deductStorage(@RequestBody DeductRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("收到扣减库存请求: commodityCode={}, count={}", 
                    request.getCommodityCode(), request.getCount());
            
            storageService.deductStorage(request.getCommodityCode(), request.getCount());

            response.put("code", 200);
            response.put("message", "库存扣减成功");
            response.put("data", null);
        } catch (Exception e) {
            logger.error("扣减库存失败", e);
            response.put("code", 500);
            response.put("message", e.getMessage());
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 查询库存
     */
    @GetMapping("/{commodityCode}")
    public Map<String, Object> getStorage(@PathVariable String commodityCode) {
        Map<String, Object> response = new HashMap<>();

        StorageEntity storage = storageService.getStorage(commodityCode);

        if (storage != null) {
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", storage);
        } else {
            response.put("code", 404);
            response.put("message", "商品不存在");
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 扣减库存请求
     */
    public static class DeductRequest {
        private String commodityCode;
        private Integer count;

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
    }
}
