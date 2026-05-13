package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 轮询负载均衡策略实现
 * 按权重依次循环选择 Provider，保证请求均匀分布
 * 
 * Dubbo 负载均衡配置:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - group = "balance-roundrobin": 服务分组，标识此实例使用轮询策略
 *   - weight = 80: 服务权重值，轮询时按权重比例分配请求
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "balance-roundrobin",
        weight = 80
)
public class UserServiceRoundRobinImpl implements UserService {

    @Override
    public String userLogin(String username, String password) {
        simulateProcessing(60);
        return "[ROUNDROBIN] " + username + " 登录成功 (权重=80)";
    }

    private void simulateProcessing(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
