package com.timelsszhuang.provider.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类 (Seata Demo)
 *
 * @author timelsszhuang
 */
@Data
@Entity
@Table(name = "t_order")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单编号
     */
    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /**
     * 商品编码
     */
    @Column(name = "commodity_code", nullable = false, length = 64)
    private String commodityCode;

    /**
     * 数量
     */
    @Column(nullable = false)
    private Integer count;

    /**
     * 金额
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 订单状态: 0-创建中, 1-已完成
     */
    @Column(nullable = false)
    private Integer status;

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
        if (status == null) {
            status = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
