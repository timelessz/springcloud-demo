-- ==========================================
-- Service-Provider SQL 语句汇总
-- ==========================================
-- 该文件包含 service-provider 模块中所有数据库操作对应的 SQL 语句
-- 基于 JPA 实体和 Repository 自动生成

-- ==========================================
-- 1. 数据库和表结构创建
-- ==========================================

-- 创建订单数据库
CREATE DATABASE IF NOT EXISTS seata_order DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seata_order;

-- -------------------------------------------
-- 1.1 订单表 (t_order)
-- -------------------------------------------
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    commodity_code VARCHAR(64) NOT NULL COMMENT '商品编码',
    count INT NOT NULL DEFAULT 0 COMMENT '数量',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
    status INT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-创建中, 1-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_commodity_code (commodity_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- -------------------------------------------
-- 1.2 账户表 (t_account)
-- -------------------------------------------
DROP TABLE IF EXISTS t_account;
CREATE TABLE t_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账户ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总额度',
    used DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已用额度',
    residue DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '剩余额度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

-- -------------------------------------------
-- 1.3 库存表 (t_storage)
-- -------------------------------------------
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

-- -------------------------------------------
-- 1.4 Seata undo_log 表 (AT模式必需)
-- -------------------------------------------
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


-- ==========================================
-- 2. 初始化测试数据
-- ==========================================

-- 2.1 初始化账户数据
INSERT INTO t_account (user_id, total, used, residue) VALUES
('1', 10000.00, 0.00, 10000.00),
('2', 5000.00, 0.00, 5000.00),
('USER001', 20000.00, 0.00, 20000.00);

-- 2.2 初始化库存数据
INSERT INTO t_storage (commodity_code, total, used, residue) VALUES
('PRODUCT-001', 100, 0, 100),
('PRODUCT-002', 200, 0, 200),
('PRODUCT-003', 500, 0, 500);


-- ==========================================
-- 3. 业务操作 SQL（对应 Repository 方法）
-- ==========================================

-- -------------------------------------------
-- 3.1 订单操作 (OrderRepository)
-- -------------------------------------------

-- 创建订单 (JPA save 方法自动生成)
INSERT INTO t_order (order_no, user_id, commodity_code, count, amount, status, create_time, update_time)
VALUES ('ORD202312160001ABC', '1', 'PRODUCT-001', 2, 200.00, 0, NOW(), NOW());

-- 根据订单编号查询订单 (findByOrderNo)
SELECT * FROM t_order WHERE order_no = 'ORD202312160001ABC';

-- 更新订单状态为已完成
UPDATE t_order SET status = 1, update_time = NOW() WHERE order_no = 'ORD202312160001ABC';

-- 根据ID查询订单 (JPA findById)
SELECT * FROM t_order WHERE id = 1;

-- 查询所有订单 (JPA findAll)
SELECT * FROM t_order;

-- 根据用户ID查询订单
SELECT * FROM t_order WHERE user_id = '1';

-- 删除订单 (JPA deleteById)
DELETE FROM t_order WHERE id = 1;


-- -------------------------------------------
-- 3.2 账户操作 (AccountRepository)
-- -------------------------------------------

-- 根据用户ID查询账户 (findByUserId)
SELECT * FROM t_account WHERE user_id = '1';

-- 扣减账户余额 (deductBalance) - 对应 @Query 注解
-- 条件: 剩余额度 >= 扣减金额
UPDATE t_account
SET used = used + 100.00,
    residue = residue - 100.00,
    update_time = NOW()
WHERE user_id = '1' AND residue >= 100.00;

-- 查询账户余额
SELECT user_id, total, used, residue FROM t_account WHERE user_id = '1';

-- 增加账户余额（充值）
UPDATE t_account
SET total = total + 1000.00,
    residue = residue + 1000.00,
    update_time = NOW()
WHERE user_id = '1';


-- -------------------------------------------
-- 3.3 库存操作 (StorageRepository)
-- -------------------------------------------

-- 根据商品编码查询库存 (findByCommodityCode)
SELECT * FROM t_storage WHERE commodity_code = 'PRODUCT-001';

-- 扣减库存 (deductStorage) - 对应 @Query 注解
-- 条件: 剩余库存 >= 扣减数量
UPDATE t_storage
SET used = used + 10,
    residue = residue - 10,
    update_time = NOW()
WHERE commodity_code = 'PRODUCT-001' AND residue >= 10;

-- 查询商品库存
SELECT commodity_code, total, used, residue FROM t_storage WHERE commodity_code = 'PRODUCT-001';

-- 增加库存（补货）
UPDATE t_storage
SET total = total + 50,
    residue = residue + 50,
    update_time = NOW()
WHERE commodity_code = 'PRODUCT-001';


-- ==========================================
-- 4. 常用查询 SQL
-- ==========================================

-- 4.1 查询所有待处理订单
SELECT * FROM t_order WHERE status = 0 ORDER BY create_time DESC;

-- 4.2 查询所有已完成订单
SELECT * FROM t_order WHERE status = 1 ORDER BY update_time DESC;

-- 4.3 查询用户的所有订单
SELECT * FROM t_order WHERE user_id = '1' ORDER BY create_time DESC;

-- 4.4 查询某商品的所有订单
SELECT * FROM t_order WHERE commodity_code = 'PRODUCT-001' ORDER BY create_time DESC;

-- 4.5 统计用户订单总金额
SELECT user_id, COUNT(*) AS order_count, SUM(amount) AS total_amount
FROM t_order
WHERE user_id = '1' AND status = 1
GROUP BY user_id;

-- 4.6 查询库存不足的商品（剩余库存 < 10）
SELECT * FROM t_storage WHERE residue < 10;

-- 4.7 查询余额不足的用户（剩余额度 < 100）
SELECT * FROM t_account WHERE residue < 100;


-- ==========================================
-- 5. 数据清理 SQL（测试用）
-- ==========================================

-- 清空订单表
TRUNCATE TABLE t_order;

-- 重置账户余额
UPDATE t_account SET used = 0, residue = total, update_time = NOW();

-- 重置库存
UPDATE t_storage SET used = 0, residue = total, update_time = NOW();

-- 清空 Seata undo_log 表
TRUNCATE TABLE undo_log;


-- ==========================================
-- 6. 分布式事务测试场景 SQL
-- ==========================================

-- 场景1: 模拟下单成功
-- Step 1: 创建订单
INSERT INTO t_order (order_no, user_id, commodity_code, count, amount, status)
VALUES ('ORD-TEST-001', '1', 'PRODUCT-001', 2, 200.00, 0);

-- Step 2: 扣减库存
UPDATE t_storage SET used = used + 2, residue = residue - 2
WHERE commodity_code = 'PRODUCT-001' AND residue >= 2;

-- Step 3: 扣减余额
UPDATE t_account SET used = used + 200.00, residue = residue - 200.00
WHERE user_id = '1' AND residue >= 200.00;

-- Step 4: 更新订单状态
UPDATE t_order SET status = 1 WHERE order_no = 'ORD-TEST-001';


-- 场景2: 模拟库存不足回滚
-- 先查看当前库存
SELECT commodity_code, residue FROM t_storage WHERE commodity_code = 'PRODUCT-001';
-- 尝试扣减超过剩余库存的数量，会触发回滚


-- 场景3: 模拟余额不足回滚
-- 先查看当前余额
SELECT user_id, residue FROM t_account WHERE user_id = '1';
-- 尝试扣减超过剩余余额的金额，会触发回滚

