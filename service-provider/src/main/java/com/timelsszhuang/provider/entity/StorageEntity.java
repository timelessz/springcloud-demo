package com.timelsszhuang.provider.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存实体类 (Seata Demo)
 *
 * @author timelsszhuang
 */
@Data
@Entity
@Table(name = "t_storage")
public class StorageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品编码
     */
    @Column(name = "commodity_code", nullable = false, length = 64)
    private String commodityCode;

    /**
     * 总库存
     */
    @Column(nullable = false)
    private Integer total;

    /**
     * 已用库存
     */
    @Column(nullable = false)
    private Integer used;

    /**
     * 剩余库存
     */
    @Column(nullable = false)
    private Integer residue;

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
