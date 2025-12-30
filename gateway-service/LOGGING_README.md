# Gateway 日志配置说明

## 问题修复

### 1. 日志配置问题
**原因**: 缺少 logback 配置文件，导致日志无法正确输出到文件

**解决方案**: 
- 创建了 `logback-spring.xml` 配置文件
- 配置了控制台输出和文件输出
- 支持按日期和大小滚动日志文件

### 2. 过滤器执行顺序问题
**原因**: JwtAuthenticationFilter 和 RequestStatisticsFilter 的优先级相同(都是0)，导致执行顺序不确定

**解决方案**: 调整了过滤器优先级，确保按正确顺序执行

## 过滤器执行顺序

当前过滤器按以下顺序执行（Order 值越小越先执行）：

| Order | 过滤器 | 说明 |
|-------|--------|------|
| -200 | PreLoggingFilter | 记录请求进入信息，生成 REQUEST_ID |
| -100 | JwtAuthenticationFilter | JWT 认证，验证 Token |
| -50 | RequestStatisticsFilter | 请求统计，记录请求次数和耗时 |
| 1 | LoggingGlobalFilter | 路由处理日志 |
| LOWEST_PRECEDENCE | PostLoggingFilter | 记录响应返回信息，计算耗时 |

## 日志输出位置

### 控制台输出
- 所有日志级别（INFO、DEBUG、ERROR）都会输出到控制台
- 带颜色高亮显示

### 文件输出
日志文件保存在 `./logs` 目录下：

1. **gateway-service-info.log** - INFO 级别日志
2. **gateway-service-error.log** - ERROR 级别日志
3. **gateway-service-debug.log** - DEBUG 级别日志（包含详细调试信息）

### 日志滚动策略
- 按天滚动
- 单个文件最大 100MB
- INFO/ERROR 日志保留 30 天
- DEBUG 日志保留 7 天

## 日志级别配置

### application.yml 配置
```yaml
logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web: DEBUG
    com.timelsszhuang.gateway: DEBUG
    com.timelsszhuang.gateway.filter: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    path: ./logs
```

### logback-spring.xml 配置
- **控制台输出**: 彩色日志，格式化输出
- **文件输出**: 异步写入，提高性能
- **环境区分**: 支持 dev 和 prod 环境不同的日志配置

## 日志使用示例

### 查看实时日志
在控制台可以看到实时日志输出，包括：
- 请求进入信息（PreLogging）
- JWT 认证信息
- 请求统计信息
- 路由处理信息
- 响应返回信息（PostLogging）

### 查看文件日志
```bash
# 查看 INFO 日志
tail -f logs/gateway-service-info.log

# 查看 ERROR 日志
tail -f logs/gateway-service-error.log

# 查看 DEBUG 日志
tail -f logs/gateway-service-debug.log
```

## 测试日志输出

### 启动服务
```bash
cd gateway-service
mvn spring-boot:run
```

### 发送测试请求
```bash
# 测试登录接口（白名单，不需要 JWT）
curl -X POST http://localhost:8085/provider/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 测试需要认证的接口
curl -X GET http://localhost:8085/provider/api/user/info \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 预期日志输出
```
╔════════════════════════════════════════════════════════════════
║ [前置过滤器] 请求进入
╠════════════════════════════════════════════════════════════════
║ 请求ID      : abc123...
║ 请求时间    : 2025-12-29 10:30:00.123
║ 请求方法    : POST
║ 请求路径    : /provider/auth/login
...
╚════════════════════════════════════════════════════════════════

白名单路径，跳过JWT验证: /provider/auth/login

╔════════════════════════════════════════════════════════════════
║ [后置过滤器] 响应返回 ✅
╠════════════════════════════════════════════════════════════════
║ 请求ID      : abc123...
║ 响应状态    : 200
║ 请求耗时    : 123 ms
...
╚════════════════════════════════════════════════════════════════
```

## 注意事项

1. **日志目录权限**: 确保应用有权限在 `./logs` 目录创建和写入文件
2. **日志级别**: 生产环境建议将 DEBUG 级别改为 INFO
3. **磁盘空间**: 注意监控日志文件占用的磁盘空间
4. **异步日志**: 使用异步 appender 提高性能，但在应用异常退出时可能丢失少量日志

## 环境配置

### 开发环境
```yaml
spring:
  profiles:
    active: dev
```
- 输出 DEBUG 级别日志
- 同时输出到控制台和文件

### 生产环境
```yaml
spring:
  profiles:
    active: prod
```
- 只输出 INFO 级别日志
- 只输出到文件，不输出到控制台

## 故障排查

### 日志不输出到文件
1. 检查 `logs` 目录是否存在
2. 检查应用是否有写入权限
3. 检查 logback-spring.xml 是否在 classpath 中

### 日志级别不生效
1. 检查 application.yml 中的配置
2. 检查 logback-spring.xml 中的 logger 配置
3. 确保没有其他配置文件覆盖了日志配置

### 控制台没有彩色日志
- Windows 控制台可能不支持 ANSI 颜色
- 可以使用支持 ANSI 的终端，如 Git Bash 或 Windows Terminal

