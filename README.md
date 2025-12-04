# Spring Cloud Alibaba Demo with Nacos

这是一个基于 Spring Cloud Alibaba 的微服务演示项目，包含服务注册发现和配置管理功能。

## 项目结构

```
springcloud-demo/
├── service-provider    # 服务提供者
├── service-consumer    # 服务消费者
└── pom.xml            # 父级POM
```

## 技术栈

- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Cloud Alibaba 2022.0.0.0
- Nacos 2.x (服务注册与配置中心)

## 功能特性

1. **服务注册与发现**: 所有微服务自动注册到 Nacos
2. **配置管理**: 支持从 Nacos 动态获取配置
3. **配置热更新**: 使用 @RefreshScope 实现配置动态刷新
4. **负载均衡**: 使用 Spring Cloud LoadBalancer
5. **服务调用**: RestTemplate 通过服务名调用

## 前置条件

### 1. 安装并启动 Nacos Server

下载 Nacos Server (推荐 2.2.0 或以上版本):
```bash
# 下载地址
https://github.com/alibaba/nacos/releases

# 解压后启动 (Windows)
cd nacos/bin
startup.cmd -m standalone

# 解压后启动 (Linux/Mac)
cd nacos/bin
sh startup.sh -m standalone
```

Nacos 控制台访问地址: http://localhost:8848/nacos
- 默认用户名: nacos
- 默认密码: nacos

## 快速开始

### 1. 编译项目

```bash
mvn clean install
```

### 2. 在 Nacos 中配置应用

访问 Nacos 控制台 http://localhost:8848/nacos，在配置管理中创建以下配置:

#### 配置 1: service-provider.yaml
- Data ID: `service-provider.yaml`
- Group: `DEFAULT_GROUP`
- 配置格式: YAML
- 配置内容:
```yaml
provider:
  message: Hello from Provider - Nacos Config
  version: 2.0
```

#### 配置 2: service-consumer.yaml
- Data ID: `service-consumer.yaml`
- Group: `DEFAULT_GROUP`
- 配置格式: YAML
- 配置内容:
```yaml
consumer:
  message: Hello from Consumer - Nacos Config
  version: 2.0
```

### 3. 启动服务

#### 启动 service-provider (服务提供者)
```bash
cd service-provider
mvn spring-boot:run
```
或者运行主类: `com.timelsszhuang.provider.ServiceProviderApplication`

服务端口: 8081

#### 启动 service-consumer (服务消费者)
```bash
cd service-consumer
mvn spring-boot:run
```
或者运行主类: `com.timelsszhuang.consumer.ServiceConsumerApplication`

服务端口: 8082

## 测试接口

### Service Provider 接口

1. **获取 Provider 信息**
   ```bash
   curl http://localhost:8081/api/hello
   ```

2. **获取 Provider 配置**
   ```bash
   curl http://localhost:8081/api/config
   ```

### Service Consumer 接口

1. **获取 Consumer 信息**
   ```bash
   curl http://localhost:8082/api/hello
   ```

2. **调用 Provider 服务**
   ```bash
   curl http://localhost:8082/api/call-provider
   ```

3. **查看注册的服务列表**
   ```bash
   curl http://localhost:8082/api/services
   ```

4. **获取 Consumer 配置**
   ```bash
   curl http://localhost:8082/api/config
   ```

## 测试配置热更新

1. 访问配置接口，查看当前配置值:
   ```bash
   curl http://localhost:8081/api/config
   ```

2. 在 Nacos 控制台修改 `service-provider.yaml` 配置，例如:
   ```yaml
   provider:
     message: Hello from Provider - Updated Config
     version: 3.0
   ```

3. 点击"发布"按钮

4. 再次访问配置接口，验证配置已更新:
   ```bash
   curl http://localhost:8081/api/config
   ```

## 查看 Nacos 注册服务

访问 Nacos 控制台的"服务管理" -> "服务列表"页面:
http://localhost:8848/nacos

你应该能看到:
- service-provider (1个实例)
- service-consumer (1个实例)

## 健康检查

两个服务都集成了 Spring Boot Actuator:

- Provider: http://localhost:8081/actuator/health
- Consumer: http://localhost:8082/actuator/health

## 常见问题

### 1. 无法连接到 Nacos
- 确认 Nacos Server 已启动
- 检查配置文件中的 `server-addr` 是否正确

### 2. 配置不生效
- 确认在 Nacos 中创建了对应的配置文件
- 检查 Data ID、Group 是否匹配
- 查看应用日志确认是否成功加载配置

### 3. 服务调用失败
- 确认两个服务都已启动
- 检查 Nacos 控制台确认服务已注册
- 查看服务健康状态

## 扩展功能

### 多实例部署

可以启动多个 Provider 实例测试负载均衡:

```bash
# 实例1 (端口 8081)
cd service-provider
mvn spring-boot:run

# 实例2 (端口 8083)
cd service-provider
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
```

然后多次调用 Consumer 的 `/api/call-provider` 接口，观察负载均衡效果。

## 许可证

MIT License

