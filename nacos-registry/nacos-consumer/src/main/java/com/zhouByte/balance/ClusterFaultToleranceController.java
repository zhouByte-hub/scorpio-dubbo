package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集群容错模式测试
 * 演示 Failover / Failfast / Failsafe 三种容错策略
 */
@RestController
@RequestMapping("/cluster")
public class ClusterFaultToleranceController {

    /** Failover: 失败自动切换，重试3次，适合读操作 */
    @DubboReference(
            interfaceClass = UserService.class,
            cluster = "failover",
            retries = 3,
            timeout = 2000
    )
    private UserService failoverService;

    /** Failfast: 只调用1次，失败立即报错，适合写操作 */
    @DubboReference(
            interfaceClass = UserService.class,
            cluster = "failfast",
            timeout = 2000
    )
    private UserService failfastService;

    /** Failsafe: 失败时忽略异常，返回null，适合非核心流程 */
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
            return "[FAILFAST] " + failfastService.userLogin(username, password)
                    + "\n快速失败模式：只尝试1次，失败立即报错";
        } catch (Exception e) {
            return "[FAILFAST] 调用失败: " + e.getMessage()
                    + "\n快速失败模式：只尝试1次，失败立即报错";
        }
    }

    @GetMapping("/failsafe/{username}/{password}")
    public String testFailsafe(@PathVariable String username, @PathVariable String password) {
        String result = failsafeService.userLogin(username, password);
        return "[FAILSAFE] 调用结果: " + result
                + "\n安全失败模式：异常被忽略，不会抛出";
    }

    @GetMapping("/compare/{username}/{password}")
    public String compareAllModes(@PathVariable String username, @PathVariable String password) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 三种集群容错模式对比 ===\n\n");

        sb.append("【1. Failover（失败自动切换）】\n");
        try {
            sb.append("  结果: ").append(failoverService.userLogin(username, password)).append("\n");
        } catch (Exception e) {
            sb.append("  异常: ").append(e.getMessage()).append("\n");
        }
        sb.append("  特点: 失败后自动重试其他节点（retries=3）\n\n");

        sb.append("【2. Failfast（快速失败）】\n");
        try {
            sb.append("  结果: ").append(failfastService.userLogin(username, password)).append("\n");
        } catch (Exception e) {
            sb.append("  异常: ").append(e.getMessage()).append("\n");
        }
        sb.append("  特点: 只尝试1次，失败立即报错\n\n");

        sb.append("【3. Failsafe（安全失败）】\n");
        String safeResult = failsafeService.userLogin(username, password);
        sb.append("  结果: ").append(safeResult).append("\n");
        sb.append("  特点: 异常被忽略，返回null\n");

        return sb.toString();
    }
}
