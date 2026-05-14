

## 📖 项目简介

**Scorpio Dubbo** 是一个基于 **Apache Dubbo** 框架的微服务架构实战演示项目，旨在展示如何使用 Dubbo 构建高可用、可扩展的分布式服务系统。项目采用 **Maven 多模块** 结构，提供三种主流注册中心的完整实现方案：**Nacos**、**Zookeeper** 和 **Redis**。

本项目适合用于：
- 学习 Apache Dubbo 微服务开发最佳实践
- 对比不同注册中心（Nacos vs Zookeeper vs Redis）的使用场景和配置差异
- 理解 RPC 远程调用、服务注册与发现、负载均衡等核心概念
- 作为企业级微服务项目的参考模板

---

## ✨ 核心特性

### 🏗️ 架构设计
- ✅ **标准化的 Maven 父子模块结构** - 三层模块层级（根模块 → 注册中心模块 → 业务子模块）
- ✅ **统一的依赖管理** - groupId: `com.scorpio`，版本集中管控
- ✅ **三层业务架构** - Interface API → Provider → Consumer 标准分层模式
- ✅ **三注册中心支持** - Nacos、Zookeeper 和 Redis 可独立运行，互不影响

### 🔧 技术亮点
- ✅ **RPC 远程调用** - 基于 Dubbo 协议的高性能服务调用
- ✅ **服务自动发现** - 动态服务注册与健康检查
- ✅ **负载均衡策略** - 支持随机、轮询、最少活跃、一致性哈希
- ✅ **集群容错机制** - Failover、Failfast、Failsafe 等容错模式
- ✅ **服务降级与 Mock** - Provider 不可用时自动降级到本地 Mock 类
- ✅ **参数验证** - 基于 JSR303/Hibernate Validator 的参数校验
- ✅ **结果缓存** - LRU/ThreadLocal/JCache 多种缓存策略
- ✅ **异步调用** - CompletableFuture 异步非阻塞调用
- ✅ **泛化调用** - 无需依赖接口 JAR 的动态调用
- ✅ **可观测性** - 链路追踪、指标收集、日志记录
- ✅ **服务治理** - 版本路由、分组路由、灰度发布
- ✅ **动态配置** - 方法级配置、自定义参数传递
- ✅ **配置中心集成** - Nacos 配置管理支持（namespace 隔离）

---

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Java** | JDK 17 | 编程语言 |
| **Spring Boot** | 2.7.17 / 3.2.5 | 应用框架基础 |
| **Apache Dubbo** | 3.2.12 / 2.7.17 | RPC 服务框架 |
| **Alibaba Nacos** | 2.x | 注册中心 + 配置中心 |
| **Apache Zookeeper** | 3.x | 分布式协调服务 |
| **Redis** | 6.x+ | 内存数据库/注册中心 |
| **Spring Cloud Alibaba** | 2021.0.5.0 | 云原生工具集 |
| **Lombok** | 1.18.32 | 代码简化工具 |
| **Maven** | 3.x | 项目构建管理 |

---

## 📁 项目结构

```
scorpio-dubbo/
│
├── pom.xml                              # 根 POM (父模块)
├── README.md                            # 项目文档
├── .gitignore                           # Git 忽略规则
├── mvnw                                 # Maven Wrapper (macOS/Linux)
├── .mvn/wrapper/                        # Maven Wrapper 配置
│
├── 📂 nacos-registry/                   # 🔵 Nacos 注册中心模块
│   ├── pom.xml                          #    中间层 POM
│   │
│   ├── 📂 nacos-interface-api/          #    接口定义层
│   │   ├── pom.xml
│   │   └── src/main/java/com/zhouByte/
│   │       ├── api/                     #       基础服务接口
│   │       │   ├── UserService.java     #          用户服务
│   │       │   └── OrderService.java    #          订单服务
│   │       ├── entity/User.java         #       用户实体类
│   │       ├── advanced/                #       高级功能接口
│   │       │   └── AsyncUserService.java #         异步服务
│   │       ├── fallback/                #       服务降级
│   │       │   ├── OrderService.java    #          订单服务接口
│   │       │   └── OrderServiceMock.java #         Mock降级类
│   │       ├── validation/              #       参数验证
│   │       │   ├── UserRegisterService.java #      注册服务
│   │       │   └── UserProfile.java     #          用户DTO
│   │       ├── cache/                   #       结果缓存
│   │       │   └── ProductQueryService.java #      商品查询
│   │       └── observability/           #       可观测性
│   │           └── MetricsQueryService.java #      指标查询
│   │
│   ├── 📂 nacos-provider/               #    服务提供者
│   │   ├── pom.xml
│   │   └── src/main/java/com/zhouByte/
│   │       ├── NacosProviderApplication.java  # 启动类
│   │       ├── service/                 #       基础服务
│   │       │   └── UserServiceImpl.java #          用户服务实现
│   │       ├── governance/              #       服务治理
│   │       │   ├── UserServiceV1.java   #          V1.0.0 基础版本
│   │       │   ├── UserServiceV2.java   #          V2.0.0 升级版本
│   │       │   └── UserServiceV3Canary.java #     V3.0.0 灰度版本
│   │       ├── balance/                 #       负载均衡
│   │       │   ├── UserServiceRandomImpl.java      # 随机策略
│   │       │   ├── UserServiceRoundRobinImpl.java  # 轮询策略
│   │       │   ├── UserServiceLeastActiveImpl.java # 最少活跃
│   │       │   └── UserServiceConsistentHashImpl.java # 一致性哈希
│   │       ├── advanced/                #       高级调用
│   │       │   ├── AsyncUserServiceImpl.java  #     异步服务实现
│   │       │   └── GenericServiceImpl.java    #     泛化服务实现
│   │       ├── fallback/                #       服务降级
│   │       │   └── OrderServiceImpl.java    #      订单服务实现
│   │       ├── validation/              #       参数验证
│   │       │   └── UserRegisterServiceImpl.java # 注册服务实现
│   │       ├── cache/                   #       结果缓存
│   │       │   └── ProductQueryServiceImpl.java # 商品查询实现
│   │       ├── config/                  #       动态配置
│   │       │   └── ConfigurableUserService.java # 可配置服务
│   │       └── observability/           #       可观测性
│   │           ├── MonitoredUserService.java  #   监控服务
│   │           ├── MetricsQueryServiceImpl.java # 指标查询
│   │           ├── TracingFilter.java   #          链路追踪过滤器
│   │           ├── MetricsFilter.java   #          指标收集过滤器
│   │           └── LoggingFilter.java   #          日志记录过滤器
│   │   └── src/main/resources/
│   │       ├── application.yaml         #       应用配置
│   │       └── META-INF/dubbo/          #       SPI配置
│   │           └── org.apache.dubbo.rpc.Filter # 过滤器注册
│   │
│   └── 📂 nacos-consumer/               #    服务消费者
│       ├── pom.xml
│       └── src/main/java/com/zhouByte/
│           ├── NacosConsumerApplication.java  # 启动类
│           ├── controller/              #       基础控制器
│           │   └── UserController.java  #          用户控制器
│           ├── governance/              #       服务治理
│           │   └── GovernanceController.java  #   治理测试
│           ├── balance/                 #       负载均衡
│           │   ├── LoadBalanceController.java   # 负载均衡测试
│           │   ├── ClusterFaultToleranceController.java # 容错测试
│           │   └── TimeoutRetryController.java  # 超时重试测试
│           ├── advanced/                #       高级调用
│           │   └── AdvancedCallController.java  # 异步/泛化调用
│           ├── fallback/                #       服务降级
│           │   └── FallbackController.java    # 降级测试
│           ├── validation/              #       参数验证
│           │   └── ValidationController.java  # 验证测试
│           ├── cache/                   #       结果缓存
│           │   └── CacheController.java     #    缓存测试
│           ├── config/                  #       动态配置
│           │   └── DynamicConfigController.java # 配置测试
│           └── observability/           #       可观测性
│               └── ObservabilityController.java # 观测性测试
│       └── src/main/resources/
│           └── application.yaml         # 应用配置
│
└── 📂 zookeeper-registry/               # 🟢 Zookeeper 注册中心模块
    ├── pom.xml                          #    中间层 POM
    │
    ├── 📂 zookeeper-interface-api/      #    接口定义层
    │   ├── pom.xml
    │   └── src/main/java/com/zhouByte/
    │       ├── api/                     #       基础服务接口
    │       │   └── UserService.java     #          用户服务
    │       ├── entity/User.java         #       用户实体类
    │       ├── advanced/                #       高级功能接口
    │       │   └── AsyncUserService.java #         异步服务
    │       ├── fallback/                #       服务降级
    │       │   ├── OrderService.java    #          订单服务接口
    │       │   └── OrderServiceMock.java #         Mock降级类
    │       ├── validation/              #       参数验证
    │       │   ├── UserRegisterService.java #      注册服务
    │       │   └── UserProfile.java     #          用户DTO
    │       ├── cache/                   #       结果缓存
    │       │   └── ProductQueryService.java #      商品查询
    │       └── observability/           #       可观测性
    │           └── MetricsQueryService.java #      指标查询
    │
    ├── 📂 zookeeper-provider/           #    服务提供者
    │   ├── pom.xml
    │   └── src/main/java/com/zhouByte/
    │       ├── ZookeeperProviderApplication.java  # 启动类
    │       ├── service/                 #       基础服务
    │       │   └── UserServiceImpl.java #          用户服务实现
    │       ├── governance/              #       服务治理
    │       │   ├── UserServiceV1.java   #          V1.0.0 基础版本
    │       │   ├── UserServiceV2.java   #          V2.0.0 升级版本
    │       │   └── UserServiceV3Canary.java #     V3.0.0 灰度版本
    │       ├── balance/                 #       负载均衡
    │       │   ├── UserServiceRandomImpl.java      # 随机策略
    │       │   ├── UserServiceRoundRobinImpl.java  # 轮询策略
    │       │   ├── UserServiceLeastActiveImpl.java # 最少活跃
    │       │   └── UserServiceConsistentHashImpl.java # 一致性哈希
    │       ├── advanced/                #       高级调用
    │       │   ├── AsyncUserServiceImpl.java  #     异步服务实现
    │       │   └── GenericServiceImpl.java    #     泛化服务实现
    │       ├── fallback/                #       服务降级
    │       │   └── OrderServiceImpl.java    #      订单服务实现
    │       ├── validation/              #       参数验证
    │       │   └── UserRegisterServiceImpl.java # 注册服务实现
    │       ├── cache/                   #       结果缓存
    │       │   └── ProductQueryServiceImpl.java # 商品查询实现
    │       ├── config/                  #       动态配置
    │       │   └── ConfigurableUserService.java # 可配置服务
    │       └── observability/           #       可观测性
    │           ├── MonitoredUserService.java  #   监控服务
    │           ├── MetricsQueryServiceImpl.java # 指标查询
    │           ├── TracingFilter.java   #          链路追踪过滤器
    │           ├── MetricsFilter.java   #          指标收集过滤器
    │           └── LoggingFilter.java   #          日志记录过滤器
    │   └── src/main/resources/
    │       ├── application.yaml         #       应用配置
    │       └── META-INF/dubbo/          #       SPI配置
    │           └── org.apache.dubbo.rpc.Filter # 过滤器注册
    │
    └── 📂 zookeeper-consumer/           #    服务消费者
        ├── pom.xml
        └── src/main/java/com/zhouByte/
            ├── ZookeeperConsumerApplication.java  # 启动类
            ├── controller/              #       基础控制器
            │   └── UserController.java  #          用户控制器
            ├── governance/              #       服务治理
            │   └── GovernanceController.java  #   治理测试
            ├── balance/                 #       负载均衡
            │   ├── LoadBalanceController.java   # 负载均衡测试
            │   ├── ClusterFaultToleranceController.java # 容错测试
            │   └── TimeoutRetryController.java  # 超时重试测试
            ├── advanced/                #       高级调用
            │   └── AdvancedCallController.java  # 异步/泛化调用
            ├── fallback/                #       服务降级
            │   └── FallbackController.java    # 降级测试
            ├── validation/              #       参数验证
            │   └── ValidationController.java  # 验证测试
            ├── cache/                   #       结果缓存
            │   └── CacheController.java     #    缓存测试
            ├── config/                  #       动态配置
            │   └── DynamicConfigController.java # 配置测试
            └── observability/           #       可观测性
                └── ObservabilityController.java # 观测性测试
        └── src/main/resources/
            └── application.yaml        # 应用配置
```

---

## 🚀 快速开始

### 前置要求

- **JDK 17+** ([下载地址](https://adoptium.net/))
- **Maven 3.6+** 或 IDE 内置 Maven
- **Nacos Server 2.x** (仅 Nacos 方案需要) [下载地址](https://github.com/alibaba/nacos/releases)
- **Zookeeper 3.x** (仅 ZK 方案需要) [下载地址](https://zookeeper.apache.org/releases.html)
- **Redis 6.x+** (仅 Redis 方案需要) [下载地址](https://redis.io/download)

### 方案一：使用 Nacos 注册中心

#### 1️⃣ 启动 Nacos Server

```bash
# 单机模式启动 Nacos
sh startup.sh -m standalone

# 访问控制台
# http://localhost:8848/nacos
# 默认账号密码: nacos/nacos
```

#### 2️⃣ 创建 Namespace（可选但推荐）

在 Nacos 控制台创建命名空间：
- 命名空间 ID: `2cb71f92-6787-40bd-b9df-ccc2be92e0ec`
- 命称: `scorpio-dev`

#### 3️⃣ 启动 Provider（服务提供者）

```bash
cd nacos-registry/nacos-provider
../mvnw spring-boot:run
# 或者在 IDEA 中直接运行 NacosProviderApplication.main()
```

✅ 成功标志：
- 控制台显示 `Started NacosProviderApplication`
- 在 Nacos 控制台「服务列表」中看到 `nacos-provider` 服务

#### 4️⃣ 启动 Consumer（服务消费者）

```bash
cd nacos-registry/nacos-consumer
../mvnw spring-boot:run
# 或者在 IDEA 中直接运行 NacosConsumerApplication.main()
```

#### 5️⃣ 测试接口

**基础接口**:
```bash
# 调用用户登录接口
curl http://localhost:8082/login/admin/123456

# 预期返回:
# admin登录成功
```

**服务降级**:
```bash
# 正常调用
curl http://localhost:8082/fallback/normal/user001/P001/2

# 强制降级
curl http://localhost:8082/fallback/force/user001/P001/2
```

**参数验证**:
```bash
# 验证通过
curl http://localhost:8082/validation/register/valid

# 验证失败
curl http://localhost:8082/validation/register/invalid-username
```

**结果缓存**:
```bash
# 缓存命中测试
curl http://localhost:8082/cache/hit/P001

# 无缓存对比
curl http://localhost:8082/cache/no-cache/P001
```

**负载均衡**:
```bash
# 随机策略
curl http://localhost:8082/balance/random/admin/123456

# 轮询策略
curl http://localhost:8082/balance/roundrobin/admin/123456
```

**服务治理**:
```bash
# V1 版本
curl http://localhost:8082/governance/v1/admin/123456

# V2 版本
curl http://localhost:8082/governance/v2/admin/123456
```

---

### 方案二：使用 Zookeeper 注册中心

#### 1️⃣ 启动 Zookeeper Server

```bash
# 进入 Zookeeper 安装目录
cd /path/to/zookeeper/bin

# 启动服务
./zkServer.sh start

# 验证状态
./zkServer.sh status
```

#### 2️⃣ 启动 Provider（服务提供者）

```bash
cd zookeeper-registry/zookeeper-provider
../../mvnw spring-boot:run
# 或在 IDEA 中运行 ZookeeperProviderApplication.main()
```

✅ 成功标志：
- 控制台显示 `Started ProviderApplication`
- Dubbo 服务已注册到 Zookeeper

#### 3️⃣ 启动 Consumer（服务消费者）

```bash
cd zookeeper-registry/zookeeper-consumer
../../mvnw spring-boot:run
# 或在 IDEA 中运行 ZookeeperConsumerApplication.main()
```

#### 4️⃣ 测试接口

**基础接口**:
```bash
# 调用用户登录接口
curl http://localhost:8081/user_login/admin/123456

# 预期返回:
# admin登录成功
```

**服务降级**:
```bash
# 正常调用
curl http://localhost:8081/fallback/normal/user001/P001/2

# 强制降级
curl http://localhost:8081/fallback/force/user001/P001/2
```

**参数验证**:
```bash
# 验证通过
curl http://localhost:8081/validation/register/valid

# 验证失败
curl http://localhost:8081/validation/register/invalid-username
```

**结果缓存**:
```bash
# 缓存命中测试
curl http://localhost:8081/cache/hit/P001

# 无缓存对比
curl http://localhost:8081/cache/no-cache/P001
```

**负载均衡**:
```bash
# 随机策略
curl http://localhost:8081/balance/random/admin/123456

# 轮询策略
curl http://localhost:8081/balance/roundrobin/admin/123456
```

**服务治理**:
```bash
# V1 版本
curl http://localhost:8081/governance/v1/admin/123456

# V2 版本
curl http://localhost:8081/governance/v2/admin/123456
```

---

### 方案三：使用 Redis 注册中心

#### 1️⃣ 启动 Redis Server

```bash
# 使用 Docker 快速启动
docker run -d --name redis -p 6379:6379 redis:latest

# 或者本地启动
redis-server

# 验证连接
redis-cli ping
# 预期返回: PONG
```

#### 2️⃣ 启动 Provider（服务提供者）

```bash
cd redis-registry/redis-provider
../../mvnw spring-boot:run
# 或在 IDEA 中运行 RedisProviderApplication.main()
```

✅ 成功标志：
- 控制台显示 `Started RedisProviderApplication`
- Dubbo 服务已注册到 Redis

#### 3️⃣ 启动 Consumer（服务消费者）

```bash
cd redis-registry/redis-consumer
../../mvnw spring-boot:run
# 或在 IDEA 中运行 RedisConsumerApplication.main()
```

#### 4️⃣ 测试接口

**基础接口**:
```bash
# 调用用户登录接口
curl http://localhost:8083/login/admin/123456

# 预期返回:
# admin登录成功
```

**服务降级**:
```bash
# 正常调用
curl http://localhost:8083/fallback/normal/user001/P001/2

# 强制降级
curl http://localhost:8083/fallback/force/user001/P001/2
```

**参数验证**:
```bash
# 验证通过
curl http://localhost:8083/validation/register/valid

# 验证失败
curl http://localhost:8083/validation/register/invalid-username
```

**结果缓存**:
```bash
# 缓存命中测试
curl http://localhost:8083/cache/hit/P001

# 无缓存对比
curl http://localhost:8083/cache/no-cache/P001
```

**负载均衡**:
```bash
# 随机策略
curl http://localhost:8083/balance/random/admin/123456

# 轮询策略
curl http://localhost:8083/balance/roundrobin/admin/123456
```

**服务治理**:
```bash
# V1 版本
curl http://localhost:8083/governance/v1/admin/123456

# V2 版本
curl http://localhost:8083/governance/v2/admin/123456
```

---

## 🎯 功能模块详解

### 1. 服务降级与 Mock

当 Provider 不可用时，自动降级到本地 Mock 类，保证系统可用性。

**Provider 端**:
```java
@DubboService(
    interfaceClass = OrderService.class,
    group = "fallback-demo",
    version = "1.0.0"
)
public class OrderServiceImpl implements OrderService {
    // 正常业务逻辑
}
```

**Mock 类** (命名规则: 接口名 + Mock):
```java
public class OrderServiceMock implements OrderService {
    @Override
    public String createOrder(String userId, String productId, int quantity) {
        return "[服务降级] 订单服务不可用，已降级到本地处理";
    }
}
```

**Consumer 端**:
```java
// 失败时自动降级
@DubboReference(
    interfaceClass = OrderService.class,
    group = "fallback-demo",
    mock = "true"
)
private OrderService orderService;

// 强制降级(不调用 Provider)
@DubboReference(mock = "force:return null")
private OrderService forceFallbackOrderService;
```

**降级策略**:
| 策略 | 说明 |
|------|------|
| `mock = "true"` | 失败时自动降级 |
| `mock = "force:return null"` | 强制降级，不调用 Provider |
| `mock = "fail:return null"` | 失败时降级，返回 null |
| `mock = "com.xxx.MyMock"` | 指定自定义 Mock 类 |

**测试接口**:
```bash
# 正常调用
curl http://localhost:8082/fallback/normal/user001/P001/2

# 强制降级
curl http://localhost:8082/fallback/force/user001/P001/2
```

---

### 2. 参数验证 (Validation)

基于 JSR303/Hibernate Validator 的参数校验，确保数据合法性。

**接口定义** (验证注解标注在接口上):
```java
public interface UserRegisterService {
    String registerUser(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 字符之间")
        String username,

        @Email(message = "邮箱格式不正确")
        String email,

        @Min(value = 1, message = "年龄必须大于等于 1")
        Integer age
    );
}
```

**Provider 端** (启用验证):
```java
@DubboService(
    interfaceClass = UserRegisterService.class,
    group = "validation-demo",
    validation = "jvalidation"  // 启用 JSR303 验证
)
public class UserRegisterServiceImpl implements UserRegisterService {
    // 无需额外验证逻辑，Dubbo 自动验证
}
```

**Consumer 端**:
```java
@DubboReference(
    interfaceClass = UserRegisterService.class,
    group = "validation-demo",
    validation = "true"  // 启用 Consumer 端验证
)
private UserRegisterService userRegisterService;
```

**常用验证注解**:
| 注解 | 说明 |
|------|------|
| `@NotNull` | 不能为 null |
| `@NotBlank` | 不能为空且长度 > 0 |
| `@Size(min, max)` | 长度/大小范围 |
| `@Min/@Max` | 数值范围 |
| `@Email` | 邮箱格式 |
| `@Pattern` | 正则表达式 |

**测试接口**:
```bash
# 验证通过
curl http://localhost:8082/validation/register/valid

# 验证失败(用户名为空)
curl http://localhost:8082/validation/register/invalid-username

# 验证失败(邮箱格式错误)
curl http://localhost:8082/validation/register/invalid-email
```

---

### 3. 结果缓存 (Cache)

减少重复调用的网络开销，提升系统性能。

**Provider 端**:
```java
@DubboService(
    interfaceClass = ProductQueryService.class,
    group = "cache-demo",
    version = "1.0.0"
)
public class ProductQueryServiceImpl implements ProductQueryService {
    @Override
    public String getProductInfo(String productId) {
        // 查询数据库...
        return "商品信息: " + productId;
    }
}
```

**Consumer 端** (启用缓存):
```java
// LRU 缓存策略
@DubboReference(
    interfaceClass = ProductQueryService.class,
    group = "cache-demo",
    cache = "lru"  // 最近最少使用，默认 1000 条
)
private ProductQueryService cachedProductService;

// 无缓存(用于对比)
@DubboReference(
    interfaceClass = ProductQueryService.class,
    group = "cache-demo"
)
private ProductQueryService noCacheProductService;
```

**缓存策略**:
| 策略 | 说明 | 适用场景 |
|------|------|----------|
| `lru` | 最近最少使用，默认 1000 条 | 通用缓存场景 |
| `threadlocal` | 线程级别缓存 | 同线程多次调用 |
| `jcache` | JSR107 JCache 集成 | 需要自定义缓存配置 |

**缓存适用场景**:
- ✅ 读操作(查询): 适合缓存
- ❌ 写操作(增删改): 不适合缓存
- ✅ 数据相对静态: 适合缓存
- ❌ 数据频繁变化: 不适合缓存

**测试接口**:
```bash
# 缓存命中测试(多次调用 timestamp 相同)
curl http://localhost:8082/cache/hit/P001

# 无缓存对比(每次 timestamp 不同)
curl http://localhost:8082/cache/no-cache/P001
```

---

### 4. 异步调用 (Async)

使用 CompletableFuture 实现非阻塞异步调用。

**接口定义**:
```java
public interface AsyncUserService {
    CompletableFuture<String> asyncLogin(String username, String password);
    String syncLogin(String username, String password);
}
```

**Provider 端**:
```java
@DubboService(
    interfaceClass = AsyncUserService.class,
    group = "advanced",
    version = "1.0.0"
)
public class AsyncUserServiceImpl implements AsyncUserService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public CompletableFuture<String> asyncLogin(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            // 异步处理逻辑
            return username + " 异步登录成功";
        }, executorService);
    }
}
```

**Consumer 端**:
```java
@DubboReference(
    interfaceClass = AsyncUserService.class,
    group = "advanced",
    version = "1.0.0"
)
private AsyncUserService asyncUserService;

// 异步调用
CompletableFuture<String> future = asyncUserService.asyncLogin("admin", "123456");
future.thenAccept(result -> System.out.println(result));
```

---

### 5. 泛化调用 (Generic)

无需依赖接口 JAR，通过接口全限定名动态调用。

**Provider 端**:
```java
@DubboService(
    interfaceClass = GenericService.class,
    group = "generic",
    version = "1.0.0",
    parameters = {"interface", "com.zhouByte.api.UserService"}
)
public class GenericServiceImpl implements GenericService {
    @Override
    public Object $invoke(String method, String[] parameterTypes, Object[] args) {
        if ("userLogin".equals(method)) {
            return handleUserLogin(args);
        }
        throw new GenericException(new UnsupportedOperationException("不支持的方法: " + method));
    }
}
```

**Consumer 端** (手动创建 ReferenceConfig):
```java
ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
referenceConfig.setInterface("com.zhouByte.api.UserService");
referenceConfig.setVersion("1.0.0");
referenceConfig.setGroup("generic");
referenceConfig.setGeneric("true");  // 开启泛化调用
referenceConfig.setRegistry(new RegistryConfig("nacos://127.0.0.1:8848"));

GenericService genericService = referenceConfig.get();
Object result = genericService.$invoke("userLogin", 
    new String[]{"java.lang.String", "java.lang.String"}, 
    new Object[]{"admin", "123456"});
```

---

### 6. 可观测性 (Observability)

集成链路追踪、指标收集、日志记录三大可观测性能力。

**Filter SPI 配置** (`META-INF/dubbo/org.apache.dubbo.rpc.Filter`):
```properties
metricsFilter=com.zhouByte.observability.MetricsFilter
tracingFilter=com.zhouByte.observability.TracingFilter
loggingFilter=com.zhouByte.observability.LoggingFilter
```

**Filter 实现**:
```java
@Activate(group = {CommonConstants.PROVIDER}, order = -11000)
public class TracingFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        String traceId = UUID.randomUUID().toString();
        RpcContext.getClientAttachment().setAttachment("traceId", traceId);
        return invoker.invoke(invocation);
    }
}
```

**服务引用**:
```java
@DubboService(
    interfaceClass = UserService.class,
    group = "observability",
    filter = {"tracingFilter", "metricsFilter", "loggingFilter"}
)
public class MonitoredUserService implements UserService {
    // 自动经过三个 Filter 处理
}
```

---

### 7. 服务治理 (Governance)

支持版本路由、分组路由、灰度发布等治理能力。

**多版本共存**:
```java
// V1.0.0 基础版本
@DubboService(interfaceClass = UserService.class, version = "1.0.0", group = "governance-version")
public class UserServiceV1 implements UserService { }

// V2.0.0 升级版本
@DubboService(interfaceClass = UserService.class, version = "2.0.0", group = "governance-version")
public class UserServiceV2 implements UserService { }

// V3.0.0 灰度版本
@DubboService(interfaceClass = UserService.class, version = "3.0.0-canary", group = "governance-version")
public class UserServiceV3Canary implements UserService { }
```

**Consumer 端路由**:
```java
// 精确调用 V1
@DubboReference(version = "1.0.0", group = "governance-version")
private UserService v1UserService;

// 精确调用 V2
@DubboReference(version = "2.0.0", group = "governance-version")
private UserService v2UserService;

// 通配符调用任意版本
@DubboReference(version = "*", group = "governance-version")
private UserService anyVersionUserService;
```

---

### 8. 动态配置 (Dynamic Config)

支持方法级配置和自定义参数传递。

**Provider 端** (方法级配置):
```java
@DubboService(
    interfaceClass = UserService.class,
    group = "dynamic-config",
    version = "1.0.0",
    methods = {
        @Method(
            name = "userLogin",
            timeout = 3000,
            retries = 2,
            loadbalance = "random"
        )
    }
)
public class ConfigurableUserService implements UserService { }
```

**Consumer 端** (自定义参数):
```java
@DubboReference(
    interfaceClass = UserService.class,
    group = "dynamic-config",
    check = false,
    methods = {
        @Method(name = "userLogin", timeout = 5000, retries = 3)
    },
    parameters = {
        "timeout", "5000",
        "retries", "3"
    }
)
private UserService dynamicConfigUserService;
```

**配置优先级** (从高到低):
1. JVM `-D` 参数
2. 方法级 `@Method` 注解
3. 接口级 `@DubboReference` 注解
4. 全局配置 (`application.yaml`)
5. 注册中心动态配置 (Nacos Config)

---

## 📊 架构说明

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    Consumer (消费者层)                        │
│  ┌─────────────┐  ┌─────────────┐                           │
│  │ REST API    │  │ HTTP Client  │                           │
│  │ Controller  │  │ (curl/Postman)│                           │
│  └──────┬──────┘  └─────────────┘                           │
│         │ @DubboReference                                   │
├─────────┼───────────────────────────────────────────────────┤
│         │ Dubbo Protocol (RPC Call)                         │
│         ▼                                                   │
│              ┌──────────────┐                               │
│              │ Registry      │  ← Service Discovery         │
│              │ (Nacos/ZK)   │                               │
│              └──────────────┘                               │
│         │                                                    │
├─────────┼───────────────────────────────────────────────────┤
│         │ @DubboService                                     │
│         ▼                                                   │
│                    Provider (提供者层)                       │
│  ┌─────────────────────────────────────────┐               │
│  │ UserServiceImpl implements UserService  │               │
│  │ - userLogin(username, password)         │               │
│  └─────────────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

### 数据流说明

1. **Consumer** 通过 `@DubboReference` 注入远程服务接口
2. **Dubbo Framework** 从注册中心获取 Provider 地址列表
3. **Load Balancer** 选择一个 Provider 实例（默认随机算法）
4. **RPC Call** 发送请求到选定的 Provider
5. **Provider** 执行业务逻辑并返回结果
6. **Result** 经序列化后返回给 Consumer

---

## ⚙️ 配置说明

### Nacos Provider 关键配置

```yaml
server:
  port: 8081

spring:
  application:
    name: nacos-provider
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        namespace: 2cb71f92-6787-40bd-b9df-ccc2be92e0ec  # 命名空间隔离
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: 2cb71f92-6787-40bd-b9df-ccc2be92e0ec

dubbo:
  registry:
    address: nacos://${spring.cloud.nacos.config.server-addr}
  protocol:
    port: 20880
    name: dubbo
  scan:
    base-packages: com.zhouByte.service  # Dubbo 服务扫描包路径
```

### Zookeeper Provider 关键配置

```yaml
spring:
  application:
    name: scorpio-zk-provider

dubbo:
  protocol:
    name: dubbo
    port: -1  # 自动分配端口
  registry:
    address: zookeeper://127.0.0.1:2181
  scan:
    base-packages: com.zhouByte.service
```

### Consumer 负载均衡配置示例

```java
@DubboReference(
    loadbalance = "random",    // 负载均衡策略: random/roundrobin/leastactive/consistenthash
    cluster = "failover",      // 集群容错: failover/failfast/failsafe/forking
    retries = 2                // 重试次数
)
private UserService userService;
```

---

## 🔍 核心代码解析

### 1. 服务接口定义 (Interface API)

```java
// nacos-registry/nacos-interface-api/src/main/java/com/zhouByte/api/UserService.java
package com.zhouByte.api;

public interface UserService {
    String userLogin(String username, String password);
}
```

**设计原则**：
- 接口定义应保持简洁，只声明方法签名
- Provider 和 Consumer 都依赖此模块，确保契约一致
- 可单独打包为 JAR 供其他项目复用

### 2. 服务实现 (Provider)

```java
// nacos-registry/nacos-provider/src/main/java/com/zhouByte/service/UserServiceImpl.java
package com.zhouByte.service;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService  // 注册为 Dubbo 服务
public class UserServiceImpl implements UserService {

    @Override
    public String userLogin(String username, String password) {
        // 参数校验
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        // 业务逻辑
        return username + "登录成功";
    }
}
```

**关键注解**：
- `@DubboService`: 将实现类暴露为 Dubbo 服务
- 自动注册到配置的注册中心

### 3. 服务消费 (Consumer)

```java
// nacos-registry/nacos-consumer/src/main/java/com/zhouByte/controller/UserController.java
package com.zhouByte.controller;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @DubboReference(loadbalance = "random", retries = 2)
    private UserService userService;  // 远程服务引用（由Dubbo注入代理对象）

    @GetMapping("/login/{username}/{password}")
    public String userLogin(@PathVariable String username,
                           @PathVariable String password) {
        return userService.userLogin(username, password);  // 像调用本地方法一样调用远程服务
    }
}
```

**关键点**：
- `@DubboReference`: 注入远程服务的代理对象
- 调用方式与本地方法完全相同（透明化 RPC）
- 支持配置负载均衡、超时、重试等参数

---

## 🎯 Nacos vs Zookeeper 对比

| 特性 | Nacos | Zookeeper |
|------|-------|-----------|
| **定位** | 服务发现 + 配置管理 | 分布式协调服务 |
| **CAP理论** | AP（可用性优先） | CP（一致性优先） |
| **健康检查** | TCP/HTTP/gRPC | 心跳机制 |
| **配置中心** | ✅ 原生支持 | ❌ 需额外组件 |
| **动态路由** | ✅ 支持 | ❌ 不支持 |
| **权重调整** | ✅ 支持 | ❌ 不支持 |
| **适用场景** | 云原生、微服务 | 大数据、Hadoop生态 |
| **学习曲线** | 较低 | 较高 |
| **社区活跃度** | 高（阿里维护） | 高（Apache） |

**推荐选择**：
- **云原生/Spring Cloud 项目** → 选择 **Nacos**
- **大数据/Hadoop 生态** → 选择 **Zookeeper**

---

## 🐛 常见问题

### Q1: 启动报错 "Connection refused to Nacos"

**原因**: Nacos Server 未启动或端口错误

**解决方案**:
```bash
# 1. 检查 Nacos 是否运行
lsof -i :8848

# 2. 启动 Nacos
cd /path/to/nacos/bin
sh startup.sh -m standalone

# 3. 检查防火墙设置
```

### Q2: Consumer 调用 Provider 超时

**原因**: 网络问题或 Provider 未正常注册

**解决方案**:
```yaml
# consumer application.yaml
dubbo:
  consumer:
    timeout: 5000  # 增加超时时间（毫秒）
    retries: 2    # 设置重试次数
```

### Q3: Zookeeper 连接失败

**原因**: Zookeeper 未启动或地址错误

**解决方案**:
```bash
# 1. 检查 Zookeeper 状态
zkServer.sh status

# 2. 查看 Zookeeper 日志
tail -f /path/to/zookeeper/logs/zookeeper.out

# 3. 确认配置文件中的地址
# address: zookeeper://127.0.0.1:2181
```

### Q4: 包名修改后编译错误

**解决方案**:
```bash
# 清理并重新编译
mvn clean compile -DskipTests

# 刷新 IDE 缓存
# IntelliJ IDEA: File -> Invalidate Caches -> Invalidate and Restart
```

---

## 📈 性能优化建议

### 1. 序列化优化
```yaml
dubbo:
  protocol:
    serialization: hessian2  # 推荐 hessian2/kryo
```

### 2. 连接池配置
```yaml
dubbo:
  provider:
    threads: 200  # 根据服务器配置调整线程数
  consumer:
    actives: 100  # 最大并发调用数
```

### 3. 异步调用
```java
@DubboReference(async = true)
private UserService userService;

CompletableFuture<String> future = userService.userLoginAsync("admin", "123456");
future.thenAccept(result -> System.out.println(result));
```

---

## 🤝 贡献指南

我们非常欢迎你的贡献！请遵循以下步骤：

### 1. Fork 本仓库
```bash
git clone https://github.com/<your-username>/scorpio-dubbo.git
```

### 2. 创建功能分支
```bash
git checkout -b feature/amazing-feature
```

### 3. 提交更改
```bash
git commit -m 'feat: add amazing feature'
```

### 4. 推送到分支
```bash
git push origin feature/amazing-feature
```

### 5. 提交 Pull Request

---

## 📜 版本历史

| 版本 | 日期 | 说明 | 作者 |
|------|------|------|------|
| **0.0.1-SNAPSHOT** | 2025-05-12 | 初始版本，支持 Nacos 和 Zookeeper 双注册中心 | zhouByte |
| **0.0.2-SNAPSHOT** | 2026-05-13 | 新增服务降级、参数验证、结果缓存、异步调用、泛化调用、可观测性、服务治理、动态配置等功能，完善所有代码注释 | zhouByte |
| **0.0.3-SNAPSHOT** | 2026-05-14 | zookeeper-registry 模块同步 nacos-registry 全部功能，更新项目结构文档 | zhouByte |
| **0.0.4-SNAPSHOT** | 2026-05-14 | 新增 redis-registry 模块，支持 Redis 作为注册中心，完善三种注册中心对比 | zhouByte |

---

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源协议发布。

```
MIT License

Copyright (c) 2025 zhouByte

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

---

## 🙏 致谢

- [Apache Dubbo](https://dubbo.apache.org/) - 高性能 Java RPC 框架
- [Alibaba Nacos](https://nacos.io/) - 动态服务发现和配置管理平台
- [Apache Zookeeper](https://zookeeper.apache.org/) - 分布式协调服务
- [Spring Boot](https://spring.io/projects/spring-boot) - 快速应用开发框架

---

## 📞 联系方式

- **作者**: zhouByte
- **GitHub**: [zhouByte-hub](https://github.com/zhouByte-hub)
- **邮箱**: (待补充)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐ Star 支持一下！**

Made with ❤️ by [zhouByte](https://github.com/zhouByte-hub)

</div>
