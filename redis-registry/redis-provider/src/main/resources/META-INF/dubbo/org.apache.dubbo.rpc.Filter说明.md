# Dubbo Filter SPI 配置文件说明

## 基本信息

| 属性 | 值 |
|------|-----|
| 文件名 | `org.apache.dubbo.rpc.Filter` |
| 路径 | `META-INF/dubbo/` |

## 作用

这是 Dubbo **SPI (Service Provider Interface)** 扩展机制的配置文件，用于注册自定义的 Dubbo RPC 过滤器(Filter)。

## 配置格式

```properties
key=value
```

| 参数 | 说明 |
|------|------|
| `key` | 过滤器名称，在 `@DubboService` 或 `@Activate` 注解中引用 |
| `value` | 过滤器实现类的全限定名 |

## 当前配置

```properties
metricsFilter=com.zhouByte.observability.MetricsFilter    # 性能指标收集过滤器
tracingFilter=com.zhouByte.observability.TracingFilter    # 链路追踪过滤器
loggingFilter=com.zhouByte.observability.LoggingFilter    # 日志记录过滤器
```

## 工作原理

1. Dubbo 启动时扫描此文件，加载所有自定义 Filter
2. 通过 `ExtensionLoader` 注册到 Dubbo 扩展系统
3. 当服务指定 `filter = {"tracingFilter"}` 时，Dubbo 找到对应类
4. 根据 `@Activate` 注解的 `group/order` 决定激活时机和执行顺序
5. RPC 请求经过 Filter 链处理

## 使用示例

### 1. 实现 Filter 接口

```java
@Activate(group = {CommonConstants.PROVIDER}, order = -11000)
public class TracingFilter implements Filter {
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        // 过滤器逻辑
    }
}
```

### 2. 在此文件中注册

```properties
tracingFilter=com.zhouByte.observability.TracingFilter
```

### 3. 在服务中引用

```java
@DubboService(filter = {"tracingFilter", "metricsFilter"})
public class UserServiceImpl implements UserService { }
```

## 注意事项

- 文件必须放在 `META-INF/dubbo/` 目录下
- 文件名必须是接口全限定名: `org.apache.dubbo.rpc.Filter`
- 每行一个 Filter 配置
- 注释使用 `#` 开头
- 类必须实现 `org.apache.dubbo.rpc.Filter` 接口
- 建议配合 `@Activate` 注解使用
