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
 */
@RestController
@RequestMapping("/observability")
public class ObservabilityController {

    @DubboReference(
            interfaceClass = UserService.class,
            group = "observability",
            version = "1.0.0"
    )
    private UserService monitoredUserService;

    @DubboReference(
            interfaceClass = MetricsQueryService.class,
            group = "observability",
            version = "1.0.0"
    )
    private MetricsQueryService metricsQueryService;

    @GetMapping("/call/{username}/{password}")
    public String testObservability(@PathVariable String username, @PathVariable String password) {
        String traceId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        RpcContext.getContext().setAttachment("traceId", traceId);
        RpcContext.getContext().setAttachment("spanId", "0");
        RpcContext.getContext().setAttachment("userId", username);
        RpcContext.getContext().setAttachment("requestType", "HTTP_API");

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
