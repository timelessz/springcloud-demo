package com.timelsszhuang.provider.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体类 (Seata Demo)
 *
 * @author timelsszhuang
 */
@Data
@Entity
@Table(name = "t_account")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /**
     * 总额度
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * 已用额度
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal used;

    /**
     * 剩余额度
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal residue;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
