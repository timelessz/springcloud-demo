-- ==========================================
-- Seata 分布式事务 Demo 数据库初始化脚本
-- ==========================================

-- -------------------------------------------
-- 1. 订单数据库
-- -------------------------------------------
CREATE DATABASE IF NOT EXISTS seata_order DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seata_order;

-- 订单表
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    commodity_code VARCHAR(64) NOT NULL COMMENT '商品编码',
    count INT NOT NULL DEFAULT 0 COMMENT '数量',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '金额',
    status INT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-创建中, 1-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- Seata undo_log 表 (AT模式必需)
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id BIGINT NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context, such as serialization',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT NOT NULL COMMENT '0:normal status, 1:defense status',
    log_created DATETIME(6) NOT NULL COMMENT 'create datetime',
    log_modified DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT模式 undo log表';

-- -------------------------------------------
-- 2. 库存数据库
-- -------------------------------------------
CREATE DATABASE IF NOT EXISTS seata_storage DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seata_storage;

-- 库存表
DROP TABLE IF EXISTS t_storage;
CREATE TABLE t_storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '库存ID',
    commodity_code VARCHAR(64) NOT NULL COMMENT '商品编码',
    total INT NOT NULL DEFAULT 0 COMMENT '总库存',
    used INT NOT NULL DEFAULT 0 COMMENT '已用库存',
    residue INT NOT NULL DEFAULT 0 COMMENT '剩余库存',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_commodity_code (commodity_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 初始化库存数据
INSERT INTO t_storage (commodity_code, total, used, residue) VALUES 
('PRODUCT-001', 100, 0, 100),
('PRODUCT-002', 200, 0, 200);

-- Seata undo_log 表
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME(6) NOT NULL,
    log_modified DATETIME(6) NOT NULL,
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT模式 undo log表';

-- -------------------------------------------
-- 3. 账户数据库
-- -------------------------------------------
CREATE DATABASE IF NOT EXISTS seata_account DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seata_account;

-- 账户表
DROP TABLE IF EXISTS t_account;
CREATE TABLE t_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账户ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    total DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总额度',
    used DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已用额度',
    residue DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '剩余额度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

-- 初始化账户数据
INSERT INTO t_account (user_id, total, used, residue) VALUES 
('1', 10000.00, 0, 10000.00),
('2', 5000.00, 0, 5000.00);

-- Seata undo_log 表
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME(6) NOT NULL,
    log_modified DATETIME(6) NOT NULL,
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT模式 undo log表';

-- ==========================================
-- 完成提示
-- ==========================================
-- 请确保:
-- 1. MySQL 服务已启动
-- 2. 使用 root 或有足够权限的用户执行此脚本
-- 3. 执行命令: mysql -u root -p < init-seata-demo.sql
