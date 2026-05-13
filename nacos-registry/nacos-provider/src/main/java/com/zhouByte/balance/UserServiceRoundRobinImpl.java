package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 轮询负载均衡策略实现
 * 按权重依次循环选择 Provider，保证请求均匀分布
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
