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
 * 负载均衡策略测试控制器
 * 
 * <h2>功能说明</h2>
 * 该控制器提供了4种Dubbo负载均衡策略的REST API测试接口，
 * 用于演示和对比不同负载均衡算法的行为特征。
 * 
 * <h2>支持的负载均衡策略</h2>
 * <ol>
 *   <li><b>Random（随机）</b>- 按权重随机选择节点，适用于一般场景</li>
 *   <li><b>RoundRobin（轮询）</b>- 按顺序循环选择，保证均匀分布</li>
 *   <li><b>LeastActive（最少活跃）</b>- 选择并发最少的节点，智能调度</li>
 *   <li><b>ConsistentHash（一致性哈希）</b>- 相同参数路由到同一节点，适合有状态场景</li>
 * </ol>
 * 
 * <h2>Dubbo @DubboReference 注解详解</h2>
 * <p>每个字段都使用 @DubboReference 注解来引用远程服务：</p>
 * <ul>
 *   <li><b>interfaceClass</b>: 指定要调用的服务接口类型</li>
 *   <li><b>group</b>: 服务分组，必须与 Provider 端 @DubboService 的 group 一致才能找到对应实现</li>
 *   <li><b>loadbalance</b>: 负载均衡策略名称，可选值：random/roundrobin/leastactive/consistenthash</li>
 *   <li><b>cluster</b>: 集群容错模式，默认为 failover（失败自动切换）</li>
 * </ul>
 * 
 * <h2>测试建议</h2>
 * <p>每个接口都会连续调用5次，便于观察不同策略的选择行为差异。</p>
 * 
 * @author zhouByte
 * @version 1.0.0
 * @see UserService
 */
@RestController
@RequestMapping("/balance")
public class LoadBalanceController {

    /**
     * 随机策略的服务引用 - 使用 Random 负载均衡
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li>group="balance-random" → 匹配 Provider 端 group 为 "balance-random" 的 UserService 实现</li>
     *   <li>loadbalance="random" → 使用随机算法选择 Provider</li>
     *   <li>cluster="failover" → 失败时自动切换到其他节点重试</li>
     * </ul>
     * 
     * <p>Dubbo 会在启动时根据这些配置创建远程服务的代理对象，
     * 所有的 RPC 调用细节对开发者透明。</p>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-random",
            loadbalance = "random",
            cluster = "failover"
    )
    private UserService randomUserService;

    /**
     * 轮询策略的服务引用 - 使用 RoundRobin 负载均衡
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li>group="balance-roundrobin" → 匹配轮询策略的 Provider</li>
     *   <li>loadbalance="roundrobin" → 按顺序依次调用各个 Provider</li>
     * </ul>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-roundrobin",
            loadbalance = "roundrobin",
            cluster = "failover"
    )
    private UserService roundrobinUserService;

    /**
     * 最少活跃数策略的服务引用 - 使用 LeastActive 负载均衡
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li>group="balance-leastactive" → 匹配最少活跃数策略的 Provider</li>
     *   <li>loadbalance="leastactive" → 动态选择当前处理请求最少的节点</li>
     * </ul>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-leastactive",
            loadbalance = "leastactive",
            cluster = "failover"
    )
    private UserService leastActiveUserService;

    /**
     * 一致性哈希策略的服务引用 - 使用 ConsistentHash 负载均衡
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li>group="balance-consistenthash" → 匹配一致性哈希策略的 Provider</li>
     *   <li>loadbalance="consistenthash" → 相同参数路由到同一节点</li>
     * </ul>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-consistenthash",
            loadbalance = "consistenthash",
            cluster = "failover"
    )
    private UserService consistentHashUserService;

    /**
     * 测试随机（Random）负载均衡策略
     * 
     * <p><b>接口功能：</b></p>
     * 连续调用5次随机策略的登录接口，观察每次返回的结果。
     * 由于是随机策略，理论上每次可能路由到不同的 Provider 实例。
     * 
     * <p><b>预期结果：</b></p>
     * 返回5行结果，每行的 [RANDOM] 标识后应该显示不同的信息（如果有多个Provider实例）
     * 或者相同的信息但顺序是随机的。
     * 
     * <p><b>观察要点：</b></p>
     * <ul>
     *   <li>多次刷新页面，观察结果的分布是否均匀</li>
     *   <li>如果启动了多个 Provider 实例，应该能看到不同权重的分布效果</li>
     * </ul>
     * 
     * @param username 用户名，从URL路径中获取
     * @param password 密码，从URL路径中获取
     * @return String 包含5次调用结果的字符串，每行一个结果
     *         HTTP GET /balance/random/admin/123456
     */
    @GetMapping("/random/{username}/{password}")
    public String testRandomBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        
        /**
         * 连续调用5次，用于观察随机策略的选择行为
         * 
         * 为什么是5次？
         * - 太少（1-2次）：难以看出随机性
         * - 适中（5次）：能观察到基本的分布情况
         * - 太多（100次）：响应数据过大，影响用户体验
         */
        for (int i = 0; i < 5; i++) {
            results.add(randomUserService.userLogin(username, password));
        }
        
        return String.join("\n", results);
    }

    /**
     * 测试轮询（RoundRobin）负载均衡策略
     * 
     * <p><b>接口功能：</b></p>
     * 连续调用5次轮询策略的登录接口，验证轮询的顺序性和均匀性。
     * 
     * <p><b>预期结果：</b></p>
     * 如果有N个Provider，调用序列应该是：A→B→C→A→B...（严格按顺序循环）
     * 
     * <p><b>观察要点：</b></p>
     * 多次访问该接口，应该能看到稳定的循环模式。
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 5次轮询调用的结果
     *         HTTP GET /balance/roundrobin/admin/123456
     */
    @GetMapping("/roundrobin/{username}/{password}")
    public String testRoundRobinBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(roundrobinUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }

    /**
     * 测试最少活跃数（LeastActive）负载均衡策略
     * 
     * <p><b>接口功能：</b></p>
     * 连续调用5次最少活跃数策略的登录接口。
     * 该策略会优先选择当前正在处理请求数最少的 Provider。
     * 
     * <p><b>预期结果：</b></p>
     * 在高并发场景下，新请求会被自动分配到较空闲的节点，
     * 从而实现负载的自适应平衡。
     * 
     * <p><b>最佳观察方式：</b></p>
     * 使用压测工具（如 JMeter、Apache Bench）并发访问此接口，
     * 观察各 Provider 的活跃数变化。
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 5次最少活跃数调用的结果
     *         HTTP GET /balance/leastactive/admin/123456
     */
    @GetMapping("/leastactive/{username}/{password}")
    public String testLeastActiveBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(leastActiveUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }

    /**
     * 测试一致性哈希（ConsistentHash）负载均衡策略
     * 
     * <p><b>接口功能：</b></p>
     * 连续调用5次一致性哈希策略的登录接口。
     * 核心特性：<strong>相同参数应该始终返回相同的 hash 值</strong>（即路由到同一节点）。
     * 
     * <p><b>预期结果：</b></p>
     * 5次调用结果中的 hash 值应该完全一致！
     * 例如：
     * [CONSISTENTHASH] admin 登录成功 (hash=42, 权重=90)
     * [CONSISTENTHASH] admin 登录成功 (hash=42, 权重=90)
     * ... （重复5次，hash值相同）
     * 
     * <p><b>验证方法：</b></p>
     * <ol>
     *   <li>使用相同参数多次访问此接口</li>
     *   <li>检查每次返回的 hash 值是否一致</li>
     *   <li>更换不同的 username 参数，观察 hash 值是否改变</li>
     * </ol>
     * 
     * @param username 用户名（作为一致性哈希的键值）
     *                相同的 username 应该产生相同的 hash 结果
     * @param password 密码
     * @return String 5次一致性哈希调用的结果
     *         HTTP GET /balance/consistenthash/admin/123456
     */
    @GetMapping("/consistenthash/{username}/{password}")
    public String testConsistentHashBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(consistentHashUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }
}
