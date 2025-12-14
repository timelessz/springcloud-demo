package com.timelsszhuang.provider.repository;

import com.timelsszhuang.provider.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 账户 Repository
 *
 * @author timelsszhuang
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    /**
     * 根据用户ID查询账户
     */
    Optional<AccountEntity> findByUserId(String userId);

    /**
     * 扣减账户余额
     */
    @Modifying
    @Query("UPDATE AccountEntity a SET a.used = a.used + :amount, a.residue = a.residue - :amount WHERE a.userId = :userId AND a.residue >= :amount")
    int deductBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);
}
