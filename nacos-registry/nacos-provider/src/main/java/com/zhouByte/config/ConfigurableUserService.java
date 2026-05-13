package com.zhouByte.config;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 可动态配置的用户服务实现
 * 演示通过 Nacos 配置中心动态修改 timeout、retries、loadbalance 等参数，无需重启应用
 * 
 * Dubbo 方法级配置说明:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - group = "dynamic-config": 服务分组，用于隔离可动态配置的服务
 *   - version = "1.0.0": 服务版本号
 *   - methods = {@Method(...)}: 方法级配置数组，可对每个方法设置独立参数
 * 
 * @Method 注解说明:
 *   作用: 对服务接口中的单个方法进行细粒度配置
 *   核心参数:
 *     - name: 方法名，必须与接口中定义的方法名完全一致
 *     - timeout: 该方法调用超时时间(毫秒)，覆盖接口级 timeout 配置
 *     - retries: 该方法失败重试次数，覆盖接口级 retries 配置
 *     - loadbalance: 该方法的负载均衡策略，覆盖接口级 loadbalance 配置
 *     - actives: 该方法的最大并发执行数，用于限流
 *     - executes: 该方法在服务端最大并发执行数
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

        return "[动态配置服务] 第 " + currentCall + " 次调用\n" +
                "用户: " + username + "\n" +
                "\n当前服务配置信息:\n" +
                "- Group: dynamic-config\n" +
                "- Version: 1.0.0\n" +
                "- 可通过 Nacos 动态修改:\n" +
                "  * timeout (超时时间)\n" +
                "  * retries (重试次数)\n" +
                "  * loadbalance (负载均衡策略)\n" +
                "  * weight (权重)";
    }
}
