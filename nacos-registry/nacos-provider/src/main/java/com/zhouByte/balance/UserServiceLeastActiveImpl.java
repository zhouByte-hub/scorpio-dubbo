package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.concurrent.atomic.AtomicInteger;

@DubboService(
        interfaceClass = UserService.class,
        group = "balance-leastactive",
        weight = 60
)
public class UserServiceLeastActiveImpl implements UserService {

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
