package com.zhouByte.observability;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/observability")
public class ObservabilityController {

    @DubboReference(
            interfaceClass = UserService.class,
            group = "observability",
            version = "1.0.0"
    )
    private UserService monitoredUserService;

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
        return MetricsFilter.getMetricsReport()
                + """
                
                💡 提示:
                - 多次调用 /observability/call/{username}/{password} 后查看此页面
                - 可看到 QPS、成功率、平均响应时间等指标
                - 实际项目中应接入 Prometheus + Grafana
                """;
    }

    @GetMapping("/metrics/reset")
    public String resetMetrics() {
        MetricsFilter.resetMetrics();
        return "[已重置所有监控指标]";
    }

    @GetMapping("/tracing/demo/{username}/{password}")
    public String testTracingDemo(@PathVariable String username, @PathVariable String password) {
        return """
                [链路追踪演示]
                
                📍 调用链路:
                HTTP Request → Consumer → Dubbo RPC → Provider
                
                🔍 追踪信息:
                - TraceID: 唯一标识一次完整调用链
                - SpanID: 标识调用链中的每个节点
                - Parent SpanID: 标识父级调用
                
                📊 日志输出位置:
                - Console: 查看 TRACE 开头的日志
                - 文件: logs/dubbo-trace.log (如果配置了文件日志)
                
                🎯 实际集成建议:
                1. Zipkin - 分布式追踪系统
                2. Jaeger - 云原生追踪平台
                3. SkyWalking - APM 监控系统
                4. OpenTelemetry - 标准化遥测协议
                
                执行调用:
                """ + monitoredUserService.userLogin(username, password);
    }
}
