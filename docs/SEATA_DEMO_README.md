# Seata 分布式事务 Demo 使用说明

## 环境准备

### 1. 启动 MySQL 并创建数据库

```bash
# 使用 MySQL 客户端执行初始化脚本
mysql -u root -p < sql/init-seata-demo.sql
```

> 注意：请根据实际情况修改 `service-provider/application.yml` 中的数据库连接信息

### 2. 启动 Nacos Server

确保 Nacos Server 运行在 `localhost:8848`

### 3. 配置 Seata Server

1. 下载 Seata Server: https://github.com/seata/seata/releases
2. 修改 `conf/application.yml`，配置 Nacos 作为注册中心和配置中心
3. 将 `sql/seataServer.properties` 上传到 Nacos 配置中心:
   - Data ID: `seataServer.properties`
   - Group: `SEATA_GROUP`
   - 格式: `properties`
4. 启动 Seata Server

### 4. 启动应用

按顺序启动:
1. `gateway-service` (端口 8085)
2. `service-provider` (端口 8081)
3. `service-consumer` (端口 8082)

---

## Demo 测试

### 查看 Demo 说明

```bash
curl http://localhost:8082/api/seata/demo-info
```

### 测试用例

#### 1. 正常购买 (成功)

```bash
curl -X POST http://localhost:8082/api/seata/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","commodityCode":"PRODUCT-001","count":1,"amount":100}'
```

#### 2. 库存不足 (回滚)

```bash
curl -X POST http://localhost:8082/api/seata/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","commodityCode":"PRODUCT-001","count":1000,"amount":100}'
```

#### 3. 余额不足 (回滚)

```bash
curl -X POST http://localhost:8082/api/seata/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","commodityCode":"PRODUCT-001","count":1,"amount":100000}'
```

#### 4. 强制异常 (回滚演示)

```bash
curl -X POST "http://localhost:8082/api/seata/purchase?forceError=true" \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","commodityCode":"PRODUCT-001","count":1,"amount":100}'
```

### 查询接口

```bash
# 查询库存
curl http://localhost:8082/api/seata/storage/PRODUCT-001

# 查询账户
curl http://localhost:8082/api/seata/account/1

# 查询订单
curl http://localhost:8082/api/seata/order/{orderNo}
```

---

## 验证分布式事务

1. 执行正常购买，检查:
   - 订单表新增记录
   - 库存表 residue 减少
   - 账户表 residue 减少

2. 执行回滚场景，检查:
   - 所有数据保持不变
   - Seata Server 日志显示回滚

3. 观察日志中的 XID 传播
