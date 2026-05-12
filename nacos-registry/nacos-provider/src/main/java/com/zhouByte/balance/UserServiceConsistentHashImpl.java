package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

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
