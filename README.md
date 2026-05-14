
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

## 🎯 Dubbo 核心功能详解

### 1. 服务注册与发现

Dubbo 通过注册中心实现服务的自动注册与发现，是微服务架构的基础能力。

**工作原理**:
1. **Provider** 启动时向注册中心注册服务元数据（接口名、版本、分组、地址等）
2. **Consumer** 启动时从注册中心订阅所需服务
3. 注册中心将服务列表推送给 Consumer
4. Consumer 基于负载均衡策略选择 Provider 进行调用
5. Provider 下线时，注册中心通知 Consumer 更新服务列表

**支持的注册中心**:

| 注册中心 | 特点 | 适用场景 |
|----------|------|----------|
| **Nacos** | 功能丰富，支持配置中心，AP/CP 可切换 | 云原生环境 |
| **Zookeeper** | 稳定可靠，强一致性 (CP) | 传统企业环境 |
| **Redis** | 轻量级，高性能，AP 模型 | 已有 Redis 基础设施 |

**配置示例**:
```yaml
# Nacos 注册中心
dubbo:
  registry:
    address: nacos://127.0.0.1:8848
    parameters:
      namespace: 2cb71f92-6787-40bd-b9df-ccc2be92e0ec

# Zookeeper 注册中心
dubbo:
  registry:
    address: zookeeper://127.0.0.1:2181

# Redis 注册中心
dubbo:
  registry:
    address: redis://127.0.0.1:6379
```

---

### 2. 服务降级与 Mock

当 Provider 不可用时，自动降级到本地 Mock 类，保证系统可用性，防止雪崩效应。

**实现原理**:
Dubbo 在 Consumer 端拦截调用异常，根据配置的降级策略返回 Mock 结果。

**Provider 端配置**:
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

**Mock 类实现** (命名规则: 接口名 + Mock):
```java
public class OrderServiceMock implements OrderService {
    @Override
    public String createOrder(String userId, String productId, int quantity) {
        return "[服务降级] 订单服务不可用，已降级到本地处理";
    }
}
```

**Consumer 端配置**:
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

| 策略 | 说明 | 适用场景 |
|------|------|----------|
| `mock = "true"` | 失败时自动降级 | 非核心业务 |
| `mock = "force:return null"` | 强制降级，不调用 Provider | 功能开关 |
| `mock = "fail:return null"` | 失败时降级，返回 null | 可选功能 |
| `mock = "com.xxx.MyMock"` | 指定自定义 Mock 类 | 复杂降级逻辑 |

---

### 3. 参数验证

基于 JSR303/Hibernate Validator 的参数校验，在服务提供者端自动验证参数合法性，减少手动校验代码。

**实现原理**:
Dubbo 在 Provider 端通过 ValidationFilter 拦截调用，自动执行参数验证。

**接口定义**:
```java
public interface UserRegisterService {
    /**
     * 用户注册
     * @param username 用户名(3-20字符)
     * @param email 邮箱(必须有效)
     * @param age 年龄(18-100)
     */
    String register(
        @NotBlank(message = "用户名不能为空") String username, 
        @Email(message = "邮箱格式不正确") String email, 
        @Min(value = 18, message = "年龄必须大于18岁") 
        @Max(value = 100, message = "年龄必须小于100岁") int age
    );
}
```

**Provider 端配置**:
```java
@DubboService(validation = "true")
public class UserRegisterServiceImpl implements UserRegisterService {
    @Override
    public String register(@NotBlank String username, @Email String email, @Min(18) @Max(100) int age) {
        // 参数已自动验证，无需手动校验
        return "注册成功";
    }
}
```

**Consumer 端配置**:
```java
@DubboReference(validation = "true")
private UserRegisterService userRegisterService;
```

**常用验证注解**:

| 注解 | 说明 | 示例 |
|------|------|------|
| `@NotBlank` | 不能为空字符串 | `@NotBlank(message = "不能为空")` |
| `@Email` | 必须是有效邮箱格式 | `@Email(message = "邮箱格式不正确")` |
| `@Min` / `@Max` | 数值范围限制 | `@Min(18) @Max(100)` |
| `@Size` | 字符串/集合长度限制 | `@Size(min = 3, max = 20)` |
| `@Pattern` | 正则表达式匹配 | `@Pattern(regexp = "^[0-9]+$")` |

---

### 4. 结果缓存

Dubbo 内置结果缓存功能，减少重复调用，提升系统性能。

**实现原理**:
Dubbo 通过 CacheFilter 拦截调用，根据配置的缓存策略缓存方法调用结果。

**Provider 端配置**:
```java
@DubboService(
    interfaceClass = ProductQueryService.class,
    cache = "lru"
)
public class ProductQueryServiceImpl implements ProductQueryService {
    @Override
    public String queryProduct(String productId) {
        // 相同参数只会调用一次
        return "产品信息";
    }
}
```

**Consumer 端配置**:
```java
// LRU 缓存(最近最少使用)
@DubboReference(cache = "lru")
private ProductQueryService lruCacheService;

// ThreadLocal 缓存(线程隔离)
@DubboReference(cache = "threadlocal")
private ProductQueryService threadLocalCacheService;

// JCache 缓存(JSR107标准)
@DubboReference(cache = "jcache")
private ProductQueryService jcacheService;
```

**缓存策略**:

| 策略 | 说明 | 适用场景 |
|------|------|----------|
| `lru` | 最近最少使用淘汰 | 通用缓存场景 |
| `threadlocal` | 线程本地缓存 | 线程隔离场景 |
| `jcache` | JSR107 标准缓存 | 需要标准化缓存接口 |

---

### 5. 异步调用

基于 CompletableFuture 的异步非阻塞调用，提升系统吞吐量。

**实现原理**:
Dubbo 支持异步调用模型，Consumer 发起调用后立即返回 Future 对象，无需阻塞等待结果。

**接口定义**:
```java
public interface AsyncUserService {
    /**
     * 异步登录
     * @return CompletableFuture<String> 异步结果
     */
    CompletableFuture<String> asyncLogin(String username, String password);
}
```

**Provider 端配置**:
```java
@DubboService
public class AsyncUserServiceImpl implements AsyncUserService {
    @Override
    public CompletableFuture<String> asyncLogin(String username, String password) {
        return CompletableFuture.completedFuture(username + "登录成功");
    }
}
```

**Consumer 端配置**:
```java
@DubboReference
private AsyncUserService asyncUserService;

public String asyncCall() throws Exception {
    // 发起异步调用
    CompletableFuture<String> future = asyncUserService.asyncLogin("admin", "123456");
    
    // 继续处理其他业务
    doOtherWork();
    
    // 等待结果
    return future.get();
}
```

**异步模式**:

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| CompletableFuture | 返回 Future 对象 | 需要异步处理结果 |
| 事件通知 | oninvoke/onreturn/onthrow | 需要回调处理 |
| 参数回调 | 实现 AsyncContext | 服务端异步 |

---

### 6. 泛化调用

无需依赖接口 JAR 包，通过 GenericService 动态调用任意服务。

**实现原理**:
Dubbo 提供 GenericService 接口，允许 Consumer 通过方法名、参数类型和参数值动态调用服务。

**Consumer 端配置**:
```java
// 创建引用配置
ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
reference.setInterface("com.zhouByte.api.UserService");
reference.setVersion("1.0.0");
reference.setGeneric(true);

// 获取泛化服务
GenericService genericService = reference.get();

// 调用方法
Object result = genericService.$invoke(
    "login",                                    // 方法名
    new String[]{"java.lang.String", "java.lang.String"}, // 参数类型
    new Object[]{"admin", "123456"}             // 参数值
);
```

**使用场景**:
- 网关服务统一调用
- 测试平台动态调用
- 服务编排场景
- 跨语言调用

---

### 7. 负载均衡

Dubbo 提供多种负载均衡策略，合理分配服务请求。

**实现原理**:
Dubbo 在 Consumer 端通过 LoadBalance 扩展点实现负载均衡，根据配置的策略选择 Provider。

**Provider 端配置**:
```java
// 随机策略(默认)
@DubboService(loadbalance = "random")
public class UserServiceRandomImpl implements UserService {}

// 轮询策略
@DubboService(loadbalance = "roundrobin")
public class UserServiceRoundRobinImpl implements UserService {}

// 最少活跃调用
@DubboService(loadbalance = "leastactive")
public class UserServiceLeastActiveImpl implements UserService {}

// 一致性哈希
@DubboService(loadbalance = "consistenthash")
public class UserServiceConsistentHashImpl implements UserService {}
```

**Consumer 端配置**:
```java
// 在引用端指定负载均衡策略
@DubboReference(loadbalance = "random")
private UserService randomUserService;

@DubboReference(loadbalance = "roundrobin")
private UserService roundrobinUserService;
```

**负载均衡策略**:

| 策略 | 说明 | 适用场景 |
|------|------|----------|
| `random` | 按权重随机调用(默认) | 通用场景 |
| `roundrobin` | 轮询调用 | 均匀分配请求 |
| `leastactive` | 最少活跃调用优先 | 服务性能差异大 |
| `consistenthash` | 一致性哈希 | 相同参数路由到同一Provider |

---

### 8. 集群容错

Dubbo 提供多种集群容错模式，保证服务高可用。

**实现原理**:
Dubbo 通过 Cluster 扩展点实现集群容错，当调用失败时根据配置的策略进行处理。

**容错模式**:

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| `failover` | 失败自动切换(默认) | 读操作，重试其他Provider |
| `failfast` | 快速失败 | 写操作，立即报错 |
| `failsafe` | 失败安全 | 日志记录等，忽略异常 |
| `failback` | 失败自动恢复 | 消息通知，定时重试 |
| `forking` | 并行调用 | 实时性要求高的读操作 |

**Provider 端配置**:
```java
@DubboService(cluster = "failover", retries = 2)
public class UserServiceImpl implements UserService {}
```

**Consumer 端配置**:
```java
@DubboReference(cluster = "failover", retries = 2)
private UserService userService;
```

---

### 9. 服务治理

通过版本管理和分组管理，实现服务的路由和灰度发布。

**实现原理**:
Dubbo 通过 version 和 group 参数实现服务的路由，Consumer 只能调用匹配版本和分组的服务。

**多版本管理**:
```java
// V1.0.0 基础版本
@DubboService(version = "1.0.0")
public class UserServiceV1 implements UserService {}

// V2.0.0 升级版本
@DubboService(version = "2.0.0")
public class UserServiceV2 implements UserService {}

// V3.0.0 灰度版本
@DubboService(version = "3.0.0-canary")
public class UserServiceV3Canary implements UserService {}
```

**Consumer 端配置**:
```java
// 调用指定版本
@DubboReference(version = "1.0.0")
private UserService v1Service;

@DubboReference(version = "2.0.0")
private UserService v2Service;
```

**分组管理**:
```java
// 按业务分组
@DubboService(group = "user-service")
public class UserServiceImpl implements UserService {}

@DubboReference(group = "user-service")
private UserService userService;
```

---

### 10. 动态配置

支持方法级配置和自定义参数传递。

**实现原理**:
Dubbo 允许在 @DubboService 和 @DubboReference 中配置方法级参数，实现细粒度控制。

**Provider 端配置**:
```java
@DubboService(
    methods = {
        @Method(name = "login", timeout = 3000, retries = 2),
        @Method(name = "register", timeout = 5000, retries = 1)
    }
)
public class ConfigurableUserService implements UserService {}
```

**Consumer 端配置**:
```java
@DubboReference(
    timeout = 5000,
    retries = 2,
    check = false
)
private UserService userService;
```

---

### 11. 可观测性

通过 Filter SPI 扩展机制，实现链路追踪、指标收集和日志记录。

**实现原理**:
Dubbo 提供 Filter 扩展点，允许在服务调用前后插入自定义逻辑。

**自定义 Filter**:
```java
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class TracingFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 记录调用开始时间
        long startTime = System.currentTimeMillis();
        
        try {
            return invoker.invoke(invocation);
        } finally {
            // 记录调用耗时
            long duration = System.currentTimeMillis() - startTime;
            log.info("调用 {} 耗时 {}ms", invocation.getMethodName(), duration);
        }
    }
}
```

**注册 Filter**:
```
# META-INF/dubbo/org.apache.dubbo.rpc.Filter
tracingFilter=com.zhouByte.observability.TracingFilter
metricsFilter=com.zhouByte.observability.MetricsFilter
loggingFilter=com.zhouByte.observability.LoggingFilter
```

---

## 🔌 Dubbo 核心概念

### Dubbo 协议

Dubbo 默认使用 Dubbo 协议，基于 TCP 长连接，采用 NIO 异步通信模型：

- **单连接复用** - 单个连接支持高并发调用
- **高效序列化** - Hessian2 序列化，性能优异
- **异步通信** - 非阻塞 I/O，高吞吐量
- **负载均衡** - 客户端负载均衡，减少网络开销

### SPI 扩展机制

Dubbo 采用 SPI (Service Provider Interface) 扩展机制，支持灵活的功能定制：

```
META-INF/dubbo/
└── org.apache.dubbo.rpc.Filter  # Filter 扩展点
```

**扩展点配置**:
```
# 格式: 扩展名=全限定类名
tracingFilter=com.zhouByte.observability.TracingFilter
```

**常用扩展点**:

| 扩展点 | 说明 |
|--------|------|
| `Filter` | 过滤器扩展 |
| `LoadBalance` | 负载均衡扩展 |
| `Cluster` | 集群容错扩展 |
| `Protocol` | 协议扩展 |
| `Registry` | 注册中心扩展 |
| `Serialization` | 序列化扩展 |

---

## 📊 三种注册中心对比

| 特性 | Nacos | Zookeeper | Redis |
|------|-------|-----------|-------|
| **CAP 理论** | AP + CP 可切换 | CP (强一致性) | AP (最终一致性) |
| **健康检查** | TCP/HTTP/MySQL | 心跳检测 | TTL 过期机制 |
| **服务发现** | 推送 + 拉取 | 拉取 | 拉取 |
| **配置中心** | ✅ 支持 | ❌ 不支持 | ❌ 不支持 |
| **多语言支持** | ✅ 完善 | ✅ 完善 | ✅ 完善 |
| **部署复杂度** | 低 | 中 | 低 |
| **适用场景** | 云原生微服务 | 传统企业环境 | 轻量级服务 |

---

## 📝 版本历史

| 版本 | 日期 | 说明 | 作者 |
|------|------|------|------|
| **0.0.1-SNAPSHOT** | 2025-05-12 | 初始版本，支持 Nacos 和 Zookeeper 双注册中心 | zhouByte |
| **0.0.2-SNAPSHOT** | 2026-05-13 | 新增服务降级、参数验证、结果缓存、异步调用、泛化调用、可观测性、服务治理、动态配置等功能，完善所有代码注释 | zhouByte |
| **0.0.3-SNAPSHOT** | 2026-05-14 | zookeeper-registry 模块同步 nacos-registry 全部功能，更新项目结构文档 | zhouByte |
| **0.0.4-SNAPSHOT** | 2026-05-14 | 新增 redis-registry 模块，支持 Redis 作为注册中心，完善三种注册中心对比 | zhouByte |

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
