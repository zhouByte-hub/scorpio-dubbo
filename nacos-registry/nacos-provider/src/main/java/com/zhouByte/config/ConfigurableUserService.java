package com.zhouByte.config;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 可动态配置的用户服务实现
 * 演示通过 Nacos 配置中心动态修改 timeout、retries、loadbalance 等参数，无需重启应用
 */
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
public class ConfigurableUserService implements UserService {

    /** 调用计数器 */
    private final AtomicLong callCount = new AtomicLong(0);

    @Override
    public String userLogin(String username, String password) {
        long currentCall = callCount.incrementAndGet();

        StringBuilder result = new StringBuilder();
        result.append("[动态配置服务] 第 ").append(currentCall).append(" 次调用\n");
        result.append("用户: ").append(username).append("\n");
        result.append("\n当前服务配置信息:\n");
        result.append("- Group: dynamic-config\n");
        result.append("- Version: 1.0.0\n");
        result.append("- 可通过 Nacos 动态修改:\n");
        result.append("  * timeout (超时时间)\n");
        result.append("  * retries (重试次数)\n");
        result.append("  * loadbalance (负载均衡策略)\n");
        result.append("  * weight (权重)");

        return result.toString();
    }
}
