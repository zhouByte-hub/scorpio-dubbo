# 🚀 Dubbo 高级功能实战指南

## 📦 功能模块总览

本项目在 nacos-registry 模块中实现了 **5 大类 Dubbo 高级功能**，每类功能独立成一个包：

```
com.zhouByte/
├── balance/          # 第1包：负载均衡 + 容错 + 超时重试
├── governance/        # 第2包：服务治理能力
├── config/            # 第3包：动态配置
├── observability/     # 第4包：监控 + 链路追踪 + 日志
└── advanced/          # 第5包：异步调用 + 泛化调用
```

---

## 🎯 第1包：负载均衡 + 容错 + 超时重试 (balance)

### 1.1 负载均衡策略对比

| 策略 | 实现类 | Group | 权重 |
|------|--------|-------|------|
| **Random** (随机) | `UserServiceRandomImpl` | `balance-random` | 100 |
| **RoundRobin** (轮询) | `UserServiceRoundRobinImpl` | `balance-roundrobin` | 80 |
| **LeastActive** (最少活跃) | `UserServiceLeastActiveImpl` | `balance-leastactive` | 60 |
| **ConsistentHash** (一致性哈希) | `UserServiceConsistentHashImpl` | `balance-consistenthash` | 90 |

#### 测试接口：

```bash
# 随机负载均衡（调用5次观察分布）
curl http://localhost:8082/balance/random/admin/123456

# 轮询负载均衡（调用5次观察顺序）
curl http://localhost:8082/balance/roundrobin/admin/123456

# 最少活跃数（并发场景下效果明显）
curl http://localhost:8082/balance/leastactive/admin/123456

# 一致性哈希（相同参数路由到同一节点）
curl http://localhost:8082/balance/consistenthash/admin/123456
```

### 1.2 集群容错模式

| 模式 | 说明 | 适用场景 |
|------|------|---------|
| **Failover** | 失败自动切换，重试3次 | 读操作、幂等操作 |
| **Failfast** | 只调用一次，失败立即报错 | 写操作、非幂等操作 |
| **Failsafe** | 异常被忽略，返回null | 日志记录、审计 |

#### 测试接口：

```bash
# Failover - 失败自动切换
curl http://localhost:8082/cluster/failover/admin/123456

# Failfast - 快速失败
curl http://localhost:8082/cluster/failfast/admin/123456

# Failsafe - 安全失败
curl http://localhost:8082/cluster/failsafe/admin/123456
```

### 1.3 超时与重试机制

#### 测试接口：

```bash
# 正常超时模式 (timeout=3000ms, retries=2)
curl http://localhost:8082/timeout/normal/admin/123456

# 严格模式 (timeout=1000ms, retries=0)
curl http://localhost:8082/timeout/strict/admin/123456
```

---

## 🔧 第2包：服务治理能力 (governance)

### 2.1 版本管理

提供 **3 个版本** 的 UserService 实现：

- **V1.0.0**: 基础版本 (MD5加密)
- **V2.0.0**: 升级版本 (BCrypt加密)
- **V3.0.0-CANARY**: 金丝雀版本 (RSA加密 + AI风控)

#### 测试接口：

```bash
# 调用 V1.0.0
curl http://localhost:8082/governance/version/v1/admin/123456

# 调用 V2.0.0
curl http://localhost:8082/governance/version/v2/admin/123456

# 匹配任意版本 (* 通配符)
curl http://localhost:8082/governance/version/any/admin/123456
```

### 2.2 标签路由

Provider V3 带有 `canary` 标签，可用于灰度发布测试。

#### 测试接口：

```bash
# 标签路由演示
curl http://localhost:8082/governance/tag/canary/admin/123456
```

### 2.3 条件路由 & 分组路由

#### 测试接口：

```bash
# 条件路由演示
curl http://localhost:8082/governance/condition/admin/123456

# 分组路由演示
curl http://localhost:8082/governance/group/demo/admin/123456
```

---

## ⚙️ 第3包：动态配置 (config)

### 特性说明

- ✅ 方法级粒度配置 (`@Method`)
- ✅ Consumer/Provider 配置覆盖机制
- ✅ 自定义参数传递
- ✅ Nacos 动态配置推送（无需重启）

#### 测试接口：

```bash
# 查看当前动态配置
curl http://localhost:8082/dynamic-config/current/admin/123456

# 配置覆盖机制演示
curl http://localhost:8082/dynamic-config/override/demo/admin/123456

# 自定义参数传递演示
curl http://localhost:8082/dynamic-config/parameters/admin/123456

# 方法级配置演示
curl http://localhost:8082/dynamic-config/method-level/admin/123456
```

### 动态配置修改方式

在 **Nacos 控制台** 添加以下配置即可实时生效：

```properties
# 修改超时时间
dubbo.consumer.com.zhouByte.api.UserService.timeout=8000

# 修改重试次数
dubbo.consumer.com.zhouByte.api.UserService.retries=5
```

---

## 📊 第4包：监控 + 链路追踪 + 日志 (observability)

### 4.1 三大 Filter 实现

| Filter | 类名 | 功能 |
|--------|------|------|
| **TracingFilter** | [TracingFilter.java](./nacos-provider/src/main/java/com/zhouByte/observability/provider/filter/TracingFilter.java) | 链路追踪 (TraceID/SpanID) |
| **MetricsFilter** | [MetricsFilter.java](./nacos-provider/src/main/java/com/zhouByte/observability/provider/filter/MetricsFilter.java) | 性能指标收集 (QPS/RT/成功率) |
| **LoggingFilter** | [LoggingFilter.java](./nacos-provider/src/main/java/com/zhouByte/observability/provider/filter/LoggingFilter.java) | 详细访问日志 |

### 4.2 监控指标

MetricsFilter 自动收集以下指标：

- 📈 总请求数 (Request Count)
- ✅ 成功数 (Success Count)
- ❌ 失败数 (Failure Count)
- 📊 成功率 (Success Rate %)
- ⏱️ 平均响应时间 (Avg Response Time ms)
- ⏰ 总耗时 (Total Time ms)

#### 测试接口：

```bash
# 执行一次调用（会触发所有 Filter）
curl http://localhost:8082/observability/call/admin/123456

# 查看监控指标报告
curl http://localhost:8082/observability/metrics

# 重置指标计数器
curl http://localhost:8082/observability/metrics/reset

# 链路追踪演示
curl http://localhost:8082/observability/tracing/demo/admin/123456
```

### 4.3 日志输出示例

调用后查看控制台日志，会看到以下格式的输出：

```
[TRACE] 开始处理请求 | traceId=a1b2c3d4e5f6g7h8 | parentSpanId=0 | currentSpanId=i9j0k1l2 | service=MonitoredUserService | method=userLogin
[DUBBO-LOG] 接收到请求 | service=com.zhouByte.api.UserService | method=userLogin | 参数=[admin, ******]
[业务日志] 开始处理用户登录 | 用户名=admin
[业务日志] 用户登录完成 | 用户名=admin
[TRACE] 请求处理完成 | traceId=a1b2c3d4e5f6g7h8 | spanId=i9j0k1l2 | 耗时=156ms | 状态=成功
[DUBBO-LOG] 请求处理完成 | service=com.zhouByte.api.UserService | method=userLogin | 耗时=156ms | 返回类型=String
```

---

## ⚡ 第5包：异步调用 + 泛化调用 (advanced)

### 5.1 异步调用 (CompletableFuture)

#### 特点：
- ✅ 不阻塞 Consumer 线程
- ✅ 返回 `CompletableFuture<String>` 对象
- ✅ 支持链式操作 (`thenApply`, `thenAccept`, `thenCompose`)
- ✅ 可并行执行多个异步任务
- ✅ 提升系统吞吐量

#### 测试接口：

```bash
# 基础异步调用
curl http://localhost:8082/advanced/async/admin/123456

# 异步链式调用 (多步骤组合)
curl http://localhost:8082/advanced/async-chain/admin/123456

# 同步 vs 异步性能对比
curl http://localhost:8082/advanced/sync-vs-async/admin/123456
```

### 5.2 泛化调用 (Generic Invocation)

#### 特点：
- ✅ 无需依赖 Provider 接口 JAR 包
- ✅ 动态指定接口名、方法名、参数类型
- ✅ 返回值类型为 Object (通常是 Map)
- ✅ 适用 API 网关、测试平台等场景

#### 测试接口：

```bash
# 泛化调用演示
curl http://localhost:8082/generic/admin/123456
```

---

## 🚀 快速启动指南

### 前置条件

1. ✅ JDK 17+
2. ✅ Maven 3.6+
3. ✅ Nacos Server (127.0.0.1:8848)
4. ✅ Namespace: `2cb71f92-6787-40bd-b9df-ccc2be92e0ec`

### 启动步骤

```bash
# 1. 编译项目
cd /Users/zhoubyte/project/java/scorpio-dubbo
mvn clean package -DskipTests

# 2. 启动 Provider (端口 8081, Dubbo 20880)
java -jar nacos-registry/nacos-provider/target/nacos-provider.jar

# 3. 启动 Consumer (端口 8082)
java -jar nacos-registry/nacos-consumer/target/nacos-consumer.jar

# 4. 访问测试接口
curl http://localhost:8082/balance/random/admin/123456
```

---

## 📝 接口总览表

| 分类 | HTTP方法 | URL路径 | 功能说明 |
|------|---------|---------|----------|
| **负载均衡** | GET | `/balance/random/{user}/{pwd}` | 随机策略 |
| | GET | `/balance/roundrobin/{user}/{pwd}` | 轮询策略 |
| | GET | `/balance/leastactive/{user}/{pwd}` | 最少活跃 |
| | GET | `/balance/consistenthash/{user}/{pwd}` | 一致性哈希 |
| **容错模式** | GET | `/cluster/failover/{user}/{pwd}` | 失败自动切换 |
| | GET | `/cluster/failfast/{user}/{pwd}` | 快速失败 |
| | GET | `/cluster/failsafe/{user}/{pwd}` | 安全失败 |
| **超时重试** | GET | `/timeout/normal/{user}/{pwd}` | 正常超时模式 |
| | GET | `/timeout/strict/{user}/{pwd}` | 严格模式 |
| **服务治理** | GET | `/governance/version/v1/{user}/{pwd}` | 版本V1 |
| | GET | `/governance/version/v2/{user}/{pwd}` | 版本V2 |
| | GET | `/governance/version/any/{user}/{pwd}` | 任意版本 |
| | GET | `/governance/tag/canary/{user}/{pwd}` | 标签路由 |
| | GET | `/governance/condition/{user}/{pwd}` | 条件路由 |
| | GET | `/governance/group/demo/{user}/{pwd}` | 分组路由 |
| **动态配置** | GET | `/dynamic-config/current/{user}/{pwd}` | 当前配置 |
| | GET | `/dynamic-config/override/demo/{user}/{pwd}` | 配置覆盖 |
| | GET | `/dynamic-config/parameters/{user}/{pwd}` | 自定义参数 |
| | GET | `/dynamic-config/method-level/{user}/{pwd}` | 方法级配置 |
| **可观测性** | GET | `/observability/call/{user}/{pwd}` | 完整可观测性 |
| | GET | `/observability/metrics` | 监控报告 |
| | GET | `/observability/metrics/reset` | 重置指标 |
| | GET | `/observability/tracing/demo/{user}/{pwd}` | 链路追踪 |
| **高级调用** | GET | `/advanced/async/{user}/{pwd}` | 异步调用 |
| | GET | `/advanced/async-chain/{user}/{pwd}` | 异步链式 |
| | GET | `/advanced/sync-vs-async/{user}/{pwd}` | 同步对比 |
| | GET | `/advanced/generic/{user}/{pwd}` | 泛化调用 |

---

## 💡 最佳实践建议

### 生产环境推荐配置

```yaml
# application.yaml
dubbo:
  provider:
    timeout: 3000
    retries: 2
    loadbalance: leastactive
    cluster: failover
  
  consumer:
    check: false
    timeout: 5000
    
  registry:
    address: nacos://${nacos.server-addr}
    
  protocol:
    name: dubbo
    port: 20880
```

### 监控集成建议

实际项目中建议接入专业监控系统：

1. **Prometheus + Grafana**
   - 导出 Metrics 数据
   - 创建可视化仪表盘
   - 设置告警规则

2. **Zipkin / Jaeger / SkyWalking**
   - 分布式链路追踪
   - 调用拓扑图
   - 性能瓶颈分析

3. **ELK Stack (Elasticsearch + Logstash + Kibana)**
   - 日志集中收集
   - 全文检索分析
   - 实时日志监控

---

## 🔗 相关文档

- [Apache Dubbo 官方文档](https://dubbo.apache.org/docs/)
- [Dubbo 3.x 新特性](https://dubbo.apache.org/docs/v3.0/user/preface/background/)
- [Nacos 注册中心文档](https://nacos.io/docs/latest/what-is-nacos/)

---

## 📄 License

MIT License
