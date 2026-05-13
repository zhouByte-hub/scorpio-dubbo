package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 一致性哈希负载均衡策略实现
 * 相同参数的请求始终路由到同一 Provider，适用于缓存、Session 保持等有状态场景
 * 
 * Dubbo 负载均衡配置:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - group = "balance-consistenthash": 服务分组，标识此实例使用一致性哈希策略
 *   - weight = 90: 服务权重值，影响哈希环上的虚拟节点数量
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "balance-consistenthash",
        weight = 90
)
public class UserServiceConsistentHashImpl implements UserService {

    @Override
    public String userLogin(String username, String password) {
        simulateProcessing(55);
        int hash = Math.abs(username.hashCode() % 100);
        return "[CONSISTENTHASH] " + username + " 登录成功 (hash=" + hash + ", 权重=90)";
    }

    private void simulateProcessing(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
