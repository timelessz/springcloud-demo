package com.timelsszhuang.provider.controller;

import com.timelsszhuang.provider.entity.AccountEntity;
import com.timelsszhuang.provider.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 账户控制器 (Seata Demo)
 *
 * @author timelsszhuang
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    /**
     * 扣减账户余额
     */
    @PostMapping("/deduct")
    public Map<String, Object> deductBalance(@RequestBody DeductRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("收到扣减余额请求: userId={}, amount={}", 
                    request.getUserId(), request.getAmount());
            
            accountService.deductBalance(request.getUserId(), request.getAmount());

            response.put("code", 200);
            response.put("message", "余额扣减成功");
            response.put("data", null);
        } catch (Exception e) {
            logger.error("扣减余额失败", e);
            response.put("code", 500);
            response.put("message", e.getMessage());
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 查询账户
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getAccount(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();

        AccountEntity account = accountService.getAccount(userId);

        if (account != null) {
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", account);
        } else {
            response.put("code", 404);
            response.put("message", "账户不存在");
            response.put("data", null);
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 扣减余额请求
     */
    public static class DeductRequest {
        private String userId;
        private BigDecimal amount;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
