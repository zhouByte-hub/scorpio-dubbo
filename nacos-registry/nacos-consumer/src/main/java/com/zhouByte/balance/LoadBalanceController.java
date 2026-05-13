package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 负载均衡策略测试
 * 演示 Random / RoundRobin / LeastActive / ConsistentHash 四种策略
 * 
 * Dubbo 负载均衡策略说明:
 * @DubboReference 配置:
 *   - loadbalance: 指定负载均衡策略
 *     1. random(加权随机): 按权重随机选择，权重越高被选中概率越大
 *     2. roundrobin(加权轮询): 按权重依次循环选择，请求分布更均匀
 *     3. leastactive(最少活跃): 优先选择当前并发请求数最少的 Provider
 *     4. consistenthash(一致性哈希): 相同参数请求路由到同一 Provider
 *   - cluster: 集群容错模式
 *   - group: 服务分组，用于隔离不同策略的服务实例
 */
@RestController
@RequestMapping("/balance")
public class LoadBalanceController {

    /**
     * 引用随机负载均衡服务
     * @DubboReference 配置:
     *   - group = "balance-random": 匹配随机策略的 Provider 分组
     *   - loadbalance = "random": 使用加权随机策略
     *   - cluster = "failover": 失败自动切换重试
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-random",
            loadbalance = "random",
            cluster = "failover"
    )
    private UserService randomUserService;

    /**
     * 引用轮询负载均衡服务
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-roundrobin",
            loadbalance = "roundrobin",
            cluster = "failover"
    )
    private UserService roundrobinUserService;

    /**
     * 引用最少活跃负载均衡服务
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-leastactive",
            loadbalance = "leastactive",
            cluster = "failover"
    )
    private UserService leastActiveUserService;

    /**
     * 引用一致性哈希负载均衡服务
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-consistenthash",
            loadbalance = "consistenthash",
            cluster = "failover"
    )
    private UserService consistentHashUserService;

    @GetMapping("/random/{username}/{password}")
    public String testRandomBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(randomUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }

    @GetMapping("/roundrobin/{username}/{password}")
    public String testRoundRobinBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(roundrobinUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }

    @GetMapping("/leastactive/{username}/{password}")
    public String testLeastActiveBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(leastActiveUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }

    @GetMapping("/consistenthash/{username}/{password}")
    public String testConsistentHashBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(consistentHashUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }
}
