package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cluster")
public class ClusterFaultToleranceController {

    @DubboReference(
            interfaceClass = UserService.class,
            cluster = "failover",
            retries = 3,
            timeout = 2000
    )
    private UserService failoverService;

    @DubboReference(
            interfaceClass = UserService.class,
            cluster = "failfast",
            timeout = 2000
    )
    private UserService failfastService;

    @DubboReference(
            interfaceClass = UserService.class,
            cluster = "failsafe",
            timeout = 2000
    )
    private UserService failsafeService;

    @GetMapping("/failover/{username}/{password}")
    public String testFailover(@PathVariable String username, @PathVariable String password) {
        return "[FAILOVER] " + failoverService.userLogin(username, password)
                + "\n失败自动切换，重试次数=3";
    }

    @GetMapping("/failfast/{username}/{password}")
    public String testFailfast(@PathVariable String username, @PathVariable String password) {
        try {
            return "[FAILFAST] " + failfastService.userLogin(username, password);
        } catch (Exception e) {
            return "[FAILFAST] 快速失败模式 - 只调用一次，立即抛出异常: " + e.getMessage();
        }
    }

    @GetMapping("/failsafe/{username}/{password}")
    public String testFailsafe(@PathVariable String username, @PathVariable String password) {
        try {
            return "[FAILSAFE] " + failsafeService.userLogin(username, password);
        } catch (Exception e) {
            return "[FAILSAFE] 安全失败模式 - 异常被忽略，返回null: " + e.getMessage();
        }
    }
}
