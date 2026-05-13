package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最少活跃数负载均衡策略实现
 * 优先选择当前并发请求数最少的 Provider，适用于 Provider 性能不均的场景
 * 活跃数相同时按权重二次选择
 * 
 * Dubbo 负载均衡配置:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - group = "balance-leastactive": 服务分组，标识此实例使用最少活跃策略
 *   - weight = 60: 服务权重值，活跃数相同时按权重分配
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "balance-leastactive",
        weight = 60
)
public class UserServiceLeastActiveImpl implements UserService {

    /** 当前活跃请求数，用于展示节点负载状态 */
    private final AtomicInteger activeCount = new AtomicInteger(0);

    @Override
    public String userLogin(String username, String password) {
        activeCount.incrementAndGet();
        try {
            simulateProcessing(40);
            return "[LEASTACTIVE] " + username + " 登录成功 (当前活跃=" + activeCount.get() + ", 权重=60)";
        } finally {
            activeCount.decrementAndGet();
        }
    }

    private void simulateProcessing(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
