package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 随机负载均衡策略实现
 * 按权重随机选择 Provider，适用于服务器性能相近的场景
 * 
 * Dubbo 负载均衡配置:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - group = "balance-random": 服务分组，标识此实例使用随机策略
 *   - weight = 100: 服务权重值(1-100)，权重越高被选中的概率越大
 *     Consumer 使用 loadbalance="random" 时会根据权重进行加权随机
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
