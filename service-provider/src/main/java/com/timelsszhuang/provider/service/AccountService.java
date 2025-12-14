package com.timelsszhuang.provider.service;

import com.timelsszhuang.provider.entity.AccountEntity;
import com.timelsszhuang.provider.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 账户服务 (Seata Demo)
 *
 * @author timelsszhuang
 */
@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    /**
     * 扣减账户余额
     *
     * @param userId 用户ID
     * @param amount 扣减金额
     */
    @Transactional
    public void deductBalance(String userId, BigDecimal amount) {
        logger.info("==> 开始扣减账户余额: userId={}, amount={}", userId, amount);

        // 查询账户
        AccountEntity account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("账户不存在: " + userId));

        // 检查余额是否充足
        if (account.getResidue().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足! 当前余额: " + account.getResidue() + ", 需要: " + amount);
        }

        // 扣减余额
        int updated = accountRepository.deductBalance(userId, amount);
        if (updated == 0) {
            throw new RuntimeException("扣减余额失败，请稍后重试");
        }

        logger.info("==> 账户余额扣减成功: userId={}, 扣减金额={}", userId, amount);
    }

    /**
     * 查询账户
     */
    public AccountEntity getAccount(String userId) {
        return accountRepository.findByUserId(userId).orElse(null);
    }
}
