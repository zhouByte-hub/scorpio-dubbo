package com.zhouByte.observability;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
