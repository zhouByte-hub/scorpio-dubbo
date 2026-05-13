package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 随机负载均衡策略实现
 * 按权重随机选择 Provider，适用于服务器性能相近的场景
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "balance-random",
        weight = 100
)
public class UserServiceRandomImpl implements UserService {

    @Override
    public String userLogin(String username, String password) {
        simulateProcessing(50);
        return "[RANDOM] " + username + " 登录成功 (权重=100)";
    }

    private void simulateProcessing(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
