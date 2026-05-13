package com.zhouByte.observability;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 可观测性用户服务 - 集成链路追踪、指标收集、日志记录
 * 
 * Dubbo 过滤器链配置:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - group = "observability": 服务分组，标识此服务启用了完整的可观测性功能
 *   - version = "1.0.0": 服务版本号
 *   - filter = {"tracingFilter", "metricsFilter", "loggingFilter"}: 
 *     指定服务过滤器链，按顺序执行
 *     过滤器说明:
 *       - tracingFilter: 链路追踪过滤器，生成/传递 traceId 和 spanId
 *       - metricsFilter: 性能指标过滤器，收集请求数、成功率、响应时间等
 *       - loggingFilter: 日志记录过滤器，记录请求参数和返回结果
 * 
 * RpcContext 说明:
 *   - RpcContext.getServerContext(): 获取服务端上下文
 *   - getAttachment(key): 获取 Consumer 端传递的附加信息(如 traceId)
 *   - setAttachment(key, value): 设置附加信息，传递给下游服务
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "observability",
        version = "1.0.0",
        filter = {"tracingFilter", "metricsFilter", "loggingFilter"}
)
public class MonitoredUserService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoredUserService.class);

    @Override
    public String userLogin(String username, String password) {
        logger.info("[业务日志] 开始处理用户登录 | 用户名={}", username);
        
        String traceId = RpcContext.getServerContext().getAttachment("traceId");
        logger.info("[业务日志] 链路追踪ID | traceId={}", traceId);

        simulateBusinessLogic();

        String result = "[可观测性服务]\n"
                + "用户: " + username + " 登录成功\n"
                + "✅ 已启用:\n"
                + "  - 链路追踪 (Tracing)\n"
                + "  - 指标收集 (Metrics)\n"
                + "  - 日志记录 (Logging)\n"
                + "\n当前 TraceID: " + traceId;

        logger.info("[业务日志] 用户登录完成 | 用户名={}", username);
        
        return result;
    }

    private void simulateBusinessLogic() {
        try {
            Thread.sleep((long) (Math.random() * 100 + 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
