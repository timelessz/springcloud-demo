# Nacos 配置管理操作指南

## 📋 配置说明

您在 `application.yml` 中定义的这部分配置：

```yaml
# 默认配置，会被Nacos配置覆盖
consumer:
  message: Hello from Consumer - Local Config
  version: 1.0
```

这些是**本地默认配置**，当 Nacos 中有相应配置时，会被 Nacos 的配置**覆盖**。

## 🚀 在 Nacos 控制台中创建/更新配置

### 步骤 1: 登录 Nacos 控制台

1. 访问: http://localhost:8848/nacos
2. 用户名: `nacos`
3. 密码: `nacos`

### 步骤 2: 进入配置管理

1. 点击左侧菜单 **"配置管理"** → **"配置列表"**
2. 确保命名空间选择为 **"public"**（或您配置的命名空间）

### 步骤 3: 创建配置

点击右上角的 **"+"** 按钮（或 **"创建配置"** 按钮）

填写以下信息：

#### 基本信息

| 字段 | 值 | 说明 |
|------|-----|------|
| **Data ID** | `service-consumer.yaml` | 必须与 application.name + file-extension 一致 |
| **Group** | `DEFAULT_GROUP` | 与 bootstrap.yml 中配置的 group 一致 |
| **配置格式** | `YAML` | 选择 YAML 格式 |

#### 配置内容

在**配置内容**文本框中输入：

```yaml
# Nacos 管理的 Consumer 配置
consumer:
  message: Hello from Nacos - Managed Config
  version: 2.0

# 可以添加其他配置
spring:
  profiles:
    active: dev
```

#### 完整示例配置

```yaml
# ===============================================
# Service Consumer 配置 (Nacos 管理)
# 最后更新: 2025-12-04
# ===============================================

consumer:
  message: Hello from Nacos - This is the production config!
  version: 2.0
  feature:
    enabled: true
    max-retry: 3

# 其他配置项
logging:
  level:
    com.timelsszhuang.consumer: DEBUG

# 可以覆盖任何 application.yml 中的配置
server:
  port: 8082
```

### 步骤 4: 发布配置

1. 填写 **"配置描述"**（可选）：例如 "Consumer 服务配置"
2. 点击 **"发布"** 按钮

### 步骤 5: 验证配置生效

配置发布后，由于 `refresh-enabled: true`，应用会**自动刷新配置**（无需重启）。

## 🔄 更新已有配置

### 方式一: 通过配置列表更新

1. 在配置列表中找到 `service-consumer.yaml`
2. 点击右侧的 **"编辑"** 按钮
3. 修改配置内容
4. 点击 **"发布"** 按钮

### 方式二: 查看详情后编辑

1. 点击配置的 **Data ID**（`service-consumer.yaml`）
2. 在详情页点击 **"编辑"** 按钮
3. 修改后点击 **"发布"**

## 🎯 配置命名规则

根据您的 `bootstrap.yml` 配置：

```yaml
spring:
  application:
    name: service-consumer  # 应用名
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: public
        group: DEFAULT_GROUP
        file-extension: yaml  # 文件扩展名
```

Nacos 会查找的配置文件：

| 配置文件名 | 说明 | 优先级 |
|-----------|------|--------|
| `service-consumer.yaml` | 基础配置 | 低 |
| `service-consumer-dev.yaml` | dev 环境配置 | 中 |
| `service-consumer-prod.yaml` | prod 环境配置 | 中 |

**Data ID 命名格式**: `${spring.application.name}.${file-extension}`

## 📊 配置生效优先级

从低到高：

1. **application.yml**（本地默认配置）
2. **Nacos 配置**（`service-consumer.yaml`）
3. **Nacos 环境配置**（`service-consumer-${profile}.yaml`）

## 🔍 验证配置是否生效

### 方法 1: 通过日志查看

启动应用后，查看控制台日志，应该看到类似：

```
Located property source: [BootstrapPropertySource {name='bootstrapProperties-service-consumer.yaml,DEFAULT_GROUP'}]
```

### 方法 2: 通过 Actuator 查看

访问: http://localhost:8082/actuator/env

搜索 `consumer.message` 或 `consumer.version`，查看值的来源。

### 方法 3: 创建测试接口

在 `ConsumerController` 中添加：

```java
@Value("${consumer.message:default}")
private String consumerMessage;

@Value("${consumer.version:1.0}")
private String consumerVersion;

@GetMapping("/config")
public Map<String, String> getConfig() {
    Map<String, String> config = new HashMap<>();
    config.put("message", consumerMessage);
    config.put("version", consumerVersion);
    config.put("source", "from Nacos or local");
    return config;
}
```

访问: http://localhost:8082/config

## 📝 配置示例

### 示例 1: 基础配置

```yaml
consumer:
  message: Hello from Nacos
  version: 2.0
```

### 示例 2: 完整配置

```yaml
consumer:
  message: Hello from Nacos - Production Environment
  version: 2.0
  retry:
    max-attempts: 3
    delay: 1000
  timeout: 5000
  
# 日志配置
logging:
  level:
    com.timelsszhuang: DEBUG
    org.springframework.cloud: INFO

# 其他配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,env,configprops
```

### 示例 3: 多环境配置

**开发环境** (`service-consumer-dev.yaml`):
```yaml
consumer:
  message: Hello from Nacos - DEV
  version: 2.0-DEV
  debug: true
```

**生产环境** (`service-consumer-prod.yaml`):
```yaml
consumer:
  message: Hello from Nacos - PROD
  version: 2.0-PROD
  debug: false
```

## 🔔 配置监听（动态刷新）

由于配置了 `refresh-enabled: true`，应用会自动监听 Nacos 配置变化。

### 在代码中使用动态配置

```java
@RefreshScope  // 添加此注解实现配置自动刷新
@RestController
public class ConsumerController {
    
    @Value("${consumer.message}")
    private String message;
    
    @Value("${consumer.version}")
    private String version;
    
    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of(
            "message", message,
            "version", version
        );
    }
}
```

更新 Nacos 配置后，**无需重启应用**，直接访问接口即可看到新值！

## ⚠️ 注意事项

1. **Data ID 必须正确**: `${application.name}.${file-extension}`
2. **Group 必须匹配**: 与 `bootstrap.yml` 中的 `group` 一致
3. **命名空间**: 确保在正确的命名空间（默认 public）
4. **格式正确**: YAML 格式要严格缩进
5. **配置发布**: 修改后必须点击"发布"才能生效

## 🎉 快速测试

1. **登录 Nacos**: http://localhost:8848/nacos
2. **创建配置**:
   - Data ID: `service-consumer.yaml`
   - Group: `DEFAULT_GROUP`
   - 内容:
     ```yaml
     consumer:
       message: Hello from Nacos Control Panel!
       version: 3.0
     ```
3. **发布配置**
4. **访问测试**: http://localhost:8082/config
5. **查看效果**: 应该看到 Nacos 的配置值

现在您可以在 Nacos 控制台中轻松管理所有配置了！

