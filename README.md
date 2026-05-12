# 🦂 Scorpio Dubbo

<table><tr><td align="center">

**基于 Apache Dubbo 的微服务架构实战项目**

<img src="https://img.shields.io/badge/Java-17-orange" alt="Java Version"/>
<img src="https://img.shields.io/badge/Spring%20Boot-2.x/3.x-green" alt="Spring Boot"/>
<img src="https://img.shields.io/badge/Apache%20Dubbo-3.x-blue" alt="Dubbo Version"/>
<img src="https://img.shields.io/badge/Nacos-2.x-red" alt="Nacos"/>
<img src="https://img.shields.io/badge/Zookeeper-3.x-yellowgreen" alt="Zookeeper"/>
<img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License"/>

</td></tr></table>

---

## 📖 项目简介

**Scorpio Dubbo** 是一个基于 **Apache Dubbo** 框架的微服务架构实战演示项目，旨在展示如何使用 Dubbo 构建高可用、可扩展的分布式服务系统。项目采用 **Maven 多模块** 结构，提供两种主流注册中心的完整实现方案：**Nacos** 和 **Zookeeper**。

本项目适合用于：
- 学习 Apache Dubbo 微服务开发最佳实践
- 对比不同注册中心（Nacos vs Zookeeper）的使用场景和配置差异
- 理解 RPC 远程调用、服务注册与发现、负载均衡等核心概念
- 作为企业级微服务项目的参考模板

---

## ✨ 核心特性

### 🏗️ 架构设计
- ✅ **标准化的 Maven 父子模块结构** - 三层模块层级（根模块 → 注册中心模块 → 业务子模块）
- ✅ **统一的依赖管理** - groupId: `com.scorpio`，版本集中管控
- ✅ **三层业务架构** - Interface API → Provider → Consumer 标准分层模式
- ✅ **双注册中心支持** - Nacos 和 Zookeeper 可独立运行，互不影响

### 🔧 技术亮点
- ✅ **RPC 远程调用** - 基于 Dubbo 协议的高性能服务调用
- ✅ **服务自动发现** - 动态服务注册与健康检查
- ✅ **负载均衡策略** - 支持随机、轮询、一致性哈希等多种算法
- ✅ **集群容错机制** - Failover、Failfast、Failsafe 等容错模式
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
| **Spring Cloud Alibaba** | 2021.0.5.0 | 云原生工具集 |
| **Lombok** | 1.18.32 | 代码简化工具 |
| **Maven** | 3.x | 项目构建管理 |

---

## 📁 项目结构

```
scorpio-dubbo/
│
├── pom.xml                              # 根 POM (父模块)
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
│   │       ├── api/UserService.java     #       用户服务接口
│   │       └── entity/User.java         #       用户实体类
│   │
│   ├── 📂 nacos-provider/               #    服务提供者
│   │   ├── pom.xml
│   │   └── src/main/java/com/zhouByte/
│   │       ├── NacosProviderApplication.java  # 启动类
│   │       └── service/UserServiceImpl.java   # 服务实现
│   │   └── src/main/resources/
│   │       └── application.yaml         # 应用配置
│   │
│   └── 📂 nacos-consumer/               #    服务消费者
│       ├── pom.xml
│       └── src/main/java/com/zhouByte/
│           ├── NacosConsumerApplication.java  # 启动类
│           └── controller/UserController.java  # REST 控制器
│       └── src/main/resources/
│           └── application.yaml         # 应用配置
│
└── 📂 zookeeper-registry/               # 🟢 Zookeeper 注册中心模块
    ├── pom.xml                          #    中间层 POM
    │
    ├── 📂 interface-api/                #    接口定义层
    │   ├── pom.xml
    │   └── src/main/java/com/zhouByte/api/
    │       └── UserService.java         #       用户服务接口
    │
    ├── 📂 provider/                     #    服务提供者
    │   ├── pom.xml
    │   └── src/main/java/com/zhouByte/
    │       ├── ProviderApplication.java  # 启动类
    │       └── service/UserServiceImpl.java  # 服务实现
    │   └── src/main/resources/
    │       └── application.yaml        # 应用配置
    │
    └── 📂 consumer/                     #    服务消费者
        ├── pom.xml
        └── src/main/java/com/zhouByte/
            ├── ConsumerApplication.java  # 启动类
            └── controller/UserController.java  # REST 控制器
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

```bash
# 调用用户登录接口
curl http://localhost:8082/login/admin/123456

# 预期返回:
# admin登录成功
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
cd zookeeper-registry/provider
../../mvnw spring-boot:run
# 或在 IDEA 中运行 ProviderApplication.main()
```

✅ 成功标志：
- 控制台显示 `Started ProviderApplication`
- Dubbo 服务已注册到 Zookeeper

#### 3️⃣ 启动 Consumer（服务消费者）

```bash
cd zookeeper-registry/consumer
../../mvnw spring-boot:run
# 或在 IDEA 中运行 ConsumerApplication.main()
```

#### 4️⃣ 测试接口

```bash
# 调用用户登录接口
curl http://localhost:8081/user_login/admin/123456

# 预期返回:
# admin登录成功
```

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
