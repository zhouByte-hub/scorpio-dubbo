package com.zhouByte.observability;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 可观测性测试
 * 演示链路追踪、性能指标采集、日志管理
 * 
 * Dubbo 可观测性说明:
 * 
 * RpcContext 上下文传递:
 *   - RpcContext.getClientAttachment(): 获取客户端附件对象(Dubbo 3.0+ 推荐方式)
 *     原 getContext() 方法已过时，推荐使用 getClientAttachment()
 *   - setAttachment(key, value): 设置键值对附加信息
 *     - 附加信息会随 RPC 调用传递到 Provider 端
 *     - 常用于传递 traceId、userId、租户ID 等上下文信息
 *   - getAttachment(key): 获取附加信息(Provider 端使用)
 * 
 * 可观测性三要素:
 *   1. 链路追踪(Tracing): 通过 traceId/spanId 追踪请求全链路
 *   2. 指标收集(Metrics): 收集请求数、成功率、响应时间等
 *   3. 日志记录(Logging): 记录请求参数、返回结果、异常信息
 */
@RestController
@RequestMapping("/observability")
public class ObservabilityController {

    /**
     * 引用可观测性服务(启用了完整过滤器链)
     * @DubboReference 配置:
     *   - interfaceClass = UserService.class: 指定服务接口
     *   - group = "observability": 匹配可观测性分组
     *   - version = "1.0.0": 匹配版本号
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "observability",
            version = "1.0.0"
    )
    private UserService monitoredUserService;

    /**
     * 引用指标查询服务
     * @DubboReference 配置:
     *   - interfaceClass = MetricsQueryService.class: 指定指标查询接口
     *   - group = "observability": 匹配可观测性分组
     *   - version = "1.0.0": 匹配版本号
     */
    @DubboReference(
            interfaceClass = MetricsQueryService.class,
            group = "observability",
            version = "1.0.0"
    )
    private MetricsQueryService metricsQueryService;

    @GetMapping("/call/{username}/{password}")
    public String testObservability(@PathVariable String username, @PathVariable String password) {
        String traceId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        RpcContext.getClientAttachment().setAttachment("traceId", traceId);
        RpcContext.getClientAttachment().setAttachment("spanId", "0");
        RpcContext.getClientAttachment().setAttachment("userId", username);
        RpcContext.getClientAttachment().setAttachment("requestType", "HTTP_API");

        return """
                [可观测性演示]
                
                已注入的上下文信息:
                - traceId: %s
                - userId: %s
                - requestType: HTTP_API
                
                Provider 端会自动记录:
                ✅ 链路追踪日志 (TRACE)
                ✅ 性能指标数据 (METRICS)
                ✅ 详细访问日志 (LOGGING)
                
                调用结果:
                """.formatted(traceId, username)
                + monitoredUserService.userLogin(username, password);
    }

    @GetMapping("/metrics")
    public String getMetricsReport() {
        return metricsQueryService.getMetricsReport()
                + "\n提示: 多次调用 /observability/call/{username}/{password} 后查看此页面";
    }

    @GetMapping("/metrics/reset")
    public String resetMetrics() {
        metricsQueryService.resetMetrics();
        return "[已重置所有监控指标]";
    }

    @GetMapping("/tracing/demo/{username}/{password}")
    public String testTracingDemo(@PathVariable String username, @PathVariable String password) {
        return """
                [链路追踪演示]
                
                调用链路: HTTP Request → Consumer → Dubbo RPC → Provider
                追踪信息: TraceID(全局唯一) + SpanID(节点标识)
                
                实际集成建议: Zipkin / Jaeger / SkyWalking / OpenTelemetry
                
                执行调用:
                """ + monitoredUserService.userLogin(username, password);
    }
}
