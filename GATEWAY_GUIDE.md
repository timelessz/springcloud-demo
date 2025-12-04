# Spring Cloud Gateway 模块说明

## 📋 模块概述

Gateway Service 是整个微服务架构的**API 网关**，提供统一的入口来访问所有后端服务。

## 🎯 核心功能

### 1. 统一入口
所有客户端请求通过 Gateway 统一进入系统，Gateway 负责路由到相应的后端服务。

### 2. 服务路由
- **自动发现**: 从 Nacos 自动发现服务
- **负载均衡**: 使用 Spring Cloud LoadBalancer 进行负载均衡
- **路径重写**: 自动去除路径前缀

### 3. 全局过滤
- **请求日志**: 记录所有通过网关的请求
- **统一鉴权**: 可扩展添加认证授权
- **跨域处理**: 统一处理 CORS
- **限流熔断**: 保护后端服务

## 🚀 快速开始

### 启动顺序

1. **启动 Nacos**
   ```batch
   start-nacos.bat
   ```

2. **启动 Provider**
   ```batch
   rebuild-start-provider.bat
   ```

3. **启动 Consumer**
   ```batch
   rebuild-start-consumer.bat
   ```

4. **启动 Gateway**
   ```batch
   rebuild-start-gateway.bat
   ```

## 🌐 访问方式

### 直接访问服务（不经过网关）

- Provider: http://localhost:8081/hello
- Consumer: http://localhost:8082/consume

### 通过网关访问

- Provider: **http://localhost:8080/provider/hello**
- Consumer: **http://localhost:8080/consumer/consume**

## 📝 路由配置说明

### 当前路由规则

在 `application.yml` 中配置：

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Provider 路由
        - id: service-provider
          uri: lb://service-provider      # lb: 负载均衡
          predicates:
            - Path=/provider/**            # 匹配路径
          filters:
            - StripPrefix=1                # 去掉 /provider 前缀
        
        # Consumer 路由
        - id: service-consumer
          uri: lb://service-consumer
          predicates:
            - Path=/consumer/**
          filters:
            - StripPrefix=1
```

### 路由规则解释

#### Provider 路由示例

**请求**: `http://localhost:8080/provider/hello`

1. 匹配规则: `Path=/provider/**` ✅
2. 去除前缀: `StripPrefix=1` → `/hello`
3. 负载均衡: `lb://service-provider`
4. 实际请求: `http://service-provider/hello`

#### Consumer 路由示例

**请求**: `http://localhost:8080/consumer/consume`

1. 匹配规则: `Path=/consumer/**` ✅
2. 去除前缀: `StripPrefix=1` → `/consume`
3. 负载均衡: `lb://service-consumer`
4. 实际请求: `http://service-consumer/consume`

## 🔧 服务发现配置

### 启用自动路由

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true                    # 启用服务发现
          lower-case-service-id: true      # 服务名小写
```

启用后，可以通过服务名直接访问：
- http://localhost:8080/service-provider/hello
- http://localhost:8080/service-consumer/consume

## 📊 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8848 | 服务注册中心 |
| **Gateway** | **8080** | **API 网关** |
| Provider | 8081 | 服务提供者 |
| Consumer | 8082 | 服务消费者 |

## 🎨 Gateway 过滤器

### 全局过滤器 - LoggingGlobalFilter

记录所有请求的详细信息：

```java
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 记录请求路径、方法、参数、耗时等
    }
}
```

**日志输出示例**:
```
========================================
Gateway 请求路径: /provider/hello
请求方法: GET
请求参数: {}
客户端地址: /0:0:0:0:0:0:0:1:51234
========================================
请求完成，耗时: 45 ms
```

## 🔍 监控端点

### Gateway Actuator 端点

访问: http://localhost:8080/actuator

可用端点：
- `/actuator/gateway/routes` - 查看所有路由
- `/actuator/gateway/routefilters` - 查看路由过滤器
- `/actuator/gateway/globalfilters` - 查看全局过滤器
- `/actuator/health` - 健康检查

### 查看路由信息

```bash
curl http://localhost:8080/actuator/gateway/routes
```

## 🎯 常见使用场景

### 1. API 聚合

通过网关统一访问所有微服务，前端只需要知道网关地址。

```
前端应用
   ↓
Gateway (8080)
   ├─→ Provider (8081)
   ├─→ Consumer (8082)
   └─→ 其他服务...
```

### 2. 统一鉴权

在 Gateway 添加认证过滤器，所有请求统一验证。

### 3. 限流保护

在 Gateway 配置限流规则，保护后端服务。

### 4. 跨域处理

统一配置 CORS，避免在每个服务中重复配置。

## 🔧 扩展配置

### 添加跨域配置

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods: "*"
            allowed-headers: "*"
```

### 添加超时配置

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5s
```

### 添加断路器

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-provider
          uri: lb://service-provider
          predicates:
            - Path=/provider/**
          filters:
            - StripPrefix=1
            - name: CircuitBreaker
              args:
                name: providerCircuitBreaker
                fallbackUri: forward:/fallback
```

## 📁 项目结构

```
gateway-service/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/timelsszhuang/gateway/
        │       ├── GatewayServiceApplication.java     # 启动类
        │       ├── config/
        │       │   └── GatewayConfig.java             # 配置类
        │       └── filter/
        │           └── LoggingGlobalFilter.java       # 全局过滤器
        └── resources/
            ├── application.yml                         # 应用配置
            └── bootstrap.yml                           # 引导配置
```

## 🧪 测试验证

### 1. 启动所有服务

确保 Nacos、Provider、Consumer、Gateway 都已启动。

### 2. 通过网关访问 Provider

```bash
curl http://localhost:8080/provider/hello
```

预期响应:
```json
{
  "message": "Hello from Nacos!",
  "port": 8081,
  "timestamp": "2025-12-04T15:30:00"
}
```

### 3. 通过网关访问 Consumer

```bash
curl http://localhost:8080/consumer/consume
```

预期响应: Consumer 调用 Provider 的结果

### 4. 查��� Gateway 日志

应该能看到请求日志：
```
Gateway 请求路径: /provider/hello
请求方法: GET
请求完成，耗时: 45 ms
```

## ⚠️ 注意事项

1. **Gateway 不能使用 spring-boot-starter-web**
   - Gateway 基于 WebFlux，使用响应式编程
   - 不要添加 Spring MVC 依赖

2. **路由顺序很重要**
   - 更具体的路由放在前面
   - 通配符路由放在后面

3. **StripPrefix 数字**
   - `StripPrefix=1` 表示去掉第一层路径
   - `/provider/hello` → `/hello`

4. **负载均衡前缀 lb://**
   - `lb://service-name` 表示从注册中心获取服务并负载均衡
   - 不需要指定具体的 host 和 port

## 🎉 完成！

Gateway Service 已成功添加到项目中！

现在您的微服务架构：
```
Nacos (8848) ← 服务注册中心
    ↑
    ├─ Gateway (8080) ← API 网关
    ├─ Provider (8081)
    └─ Consumer (8082)
```

使用 Gateway 可以实现统一入口、路由转发、负载均衡、过滤器等功能！

