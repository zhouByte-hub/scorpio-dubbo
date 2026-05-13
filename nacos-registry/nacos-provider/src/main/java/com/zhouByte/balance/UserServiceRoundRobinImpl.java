package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 轮询（Round Robin）负载均衡策略实现类
 * 
 * <h2>功能说明</h2>
 * 该类实现了 UserService 接口，使用 Dubbo 的轮询（RoundRobin）负载均衡策略。
 * 轮询策略会按照顺序依次循环选择每个服务提供者，确保每个节点获得均等的请求机会。
 * 
 * <h2>Dubbo 配置说明</h2>
 * <ul>
 *   <li><b>interfaceClass</b>: 指定服务接口类型为 UserService</li>
 *   <li><b>group</b>: 设置为 "balance-roundrobin"，表示该服务属于"轮询策略"分组
 *       Consumer 通过此 group 定位到该实现</li>
 *   <li><b>weight</b>: 权重=80，比随机策略略低
 *       在加权轮询中，权重影响每次循环中该节点被选中的次数比例
 *       例如：A(权重80) : B(权重100) = 8:10 的调用比例</li>
 * </ul>
 * 
 * <h2>轮询算法原理</h2>
 * <p>Dubbo 的 RoundRobin 算法采用<strong>加权轮询</strong>机制：</p>
 * <pre>
 * 假设有3个 Provider:
 *   A (weight=80), B (weight=100), C (weight=60)
 * 总权重 = 240
 * 
 * 调用序列示例（简化）:
 * 第1次 → A (当前权重最大)
 * 第2次 → B
 * 第3次 → C
 * 第4次 → B
 * ... 循环往复
 * </pre>
 * 
 * <h2>适用场景</h2>
 * <ul>
 *   <li>✅ 服务器性能相近，希望均匀分配请求</li>
 *   <li>✅ 无状态服务，每次请求相互独立</li>
 *   <li>✅ 需要保证每个节点都有流量通过（健康检查）</li>
 *   <li>❌ 不适合有状态的服务（如 Session 保持）</li>
 * </ul>
 * 
 * <h2>与 Random 策略对比</h2>
 * <table border="1">
 *   <tr><th>特性</th><th>Random（随机）</th><th>RoundRobin（轮询）</th></tr>
 *   <tr><td>分布均匀性</td><td>统计上均匀</td><td>严格均匀</td></tr>
 *   <tr><td>实现复杂度</td><td>O(1)</td><td>O(n)</td></tr>
 *   <tr><td>可预测性</td><td>不可预测</td><td>可预测</td></tr>
 *   <tr><td>适合场景</td><td>高并发简单场景</td><td>需严格均匀分布</td></tr>
 * </table>
 * 
 * @author zhouByte
 * @version 1.0.0
 * @see UserService
 * @see org.apache.dubbo.rpc.cluster.loadbalance.RoundRobinLoadBalance
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "balance-roundrobin",
        weight = 80
)
public class UserServiceRoundRobinImpl implements UserService {

    /**
     * 用户登录方法 - 使用轮询负载均衡策略
     * 
     * <p>该方法在轮询策略下处理用户登录请求：
     * <ol>
     *   <li>接收用户凭证（用户名+密码）</li>
     *   <li>模拟60ms的业务处理耗时（略长于随机策略，体现差异化）</li>
     *   <li>返回带有 [ROUNDROBIN] 标识的结果</li>
     * </ol>
     * 
     * <p><b>性能特点：</b><br>
     * 模拟耗时设为60ms（比Random的50ms长），原因：
     * <ul>
     *   <li>展示不同 Provider 可以有不同的响应特征</li>
     *   <li>便于在监控指标中区分不同策略的性能表现</li>
     *   <li>模拟真实环境中服务器性能不均的情况</li>
     * </ul>
     * 
     * @param username 用户名，登录用的账户标识
     * @param password 密码，用于身份验证
     * @return String 格式化的登录结果，包含策略标识和权重信息
     *         示例: "[ROUNDROBIN] admin 登录成功 (权重=80)"
     */
    @Override
    public String userLogin(String username, String password) {
        simulateProcessing(60);
        return "[ROUNDROBIN] " + username + " 登录成功 (权重=80)";
    }

    /**
     * 模拟业务处理耗时
     * 
     * @param ms 延迟毫秒数
     */
    private void simulateProcessing(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
