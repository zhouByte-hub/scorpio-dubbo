package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最少活跃数（LeastActive）负载均衡策略实现类
 * 
 * <h2>功能说明</h2>
 * 该类实现了 UserService 接口，使用 Dubbo 的最少活跃数（LeastActive）负载均衡策略。
 * 最少活跃数策略会选择当前正在处理的请求数最少的 Provider 进行调用。
 * 
 * <h2>核心原理</h2>
 * <p>什么是"活跃数"？<br>
 * 活跃数（Active Count）指的是某个 Provider 当前<strong>正在处理但尚未完成</strong>的请求数量。<br>
 * 每当收到一个新请求，活跃数+1；每当完成一个请求，活跃数-1。</p>
 * 
 * <pre>
 * 示例场景（3个Provider）:
 * ┌──────────┬──────────┬──────────┐
 * │ Provider │ 活跃数   │ 选择倾向 │
 * ├──────────┼──────────┼──────────┤
 * │ Node-A   │    5     │   低     │
 * │ Node-B   │    2     │   ⭐高   │ ← 优先选择
 * │ Node-C   │    8     │   很低   │
 * └──────────┴──────────┴──────────┘
 * 
 * 结果：新请求会被路由到 Node-B（活跃数最少）
 * </pre>
 * 
 * <h2>Dubbo 配置说明</h2>
 * <ul>
 *   <li><b>group</b>: "balance-leastactive" - 标识该服务属于最少活跃数策略组</li>
 *   <li><b>weight</b>: 60 - 权重较低，配合最少活跃数策略一起生效
 *       当多个节点活跃数相同时，按权重进行二次选择</li>
 * </ul>
 * 
 * <h2>适用场景</h2>
 * <ul>
 *   <li>✅ <strong>高性能要求场景</strong> - 快速响应用户请求</li>
 *   <li>✅ <strong>Provider 性能不均</strong> - 自动避开繁忙节点</li>
 *   <li>✅ <strong>长时间任务</strong> - 如报表导出、大数据计算</li>
 *   <li>✅ <strong>智能调度</strong> - 动态感知各节点负载情况</li>
 *   <li>❌ 不适合所有 Provider 处理速度都相同的场景</li>
 * </ul>
 * 
 * <h2>与其他策略的区别</h2>
 * <table border="1">
 *   <tr>
 *     <th>策略</th>
 *     <th>选择依据</th>
 *     <th>动态性</th>
 *     <th>适用场景</th>
 *   </tr>
 *   <tr>
 *     <td>Random</td>
 *     <td>随机（按权重）</td>
 *     <td>静态</td>
 *     <td>一般场景</td>
 *   </tr>
 *   <tr>
 *     <td>RoundRobin</td>
 *     <td>轮流（按权重）</td>
 *     <td>静态</td>
 *     <td>均匀分布</td>
 *   </tr>
 *   <tr>
 *     <td><b>LeastActive</b></td>
 *     <td><b>实时活跃数</b></td>
 *     <td><b>动态</b></td>
 *     <td><b>性能敏感</b></td>
 *   </tr>
 * </table>
 * 
 * <h2>线程安全说明</h2>
 * 使用 {@link AtomicInteger} 保证活跃数计数的线程安全性，
 * 因为在高并发环境下，多个线程可能同时修改 activeCount。
 * 
 * @author zhouByte
 * @version 1.0.0
 * @see UserService
 * @see org.apache.dubbo.rpc.cluster.loadbalance.LeastActiveLoadBalance
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "balance-leastactive",
        weight = 60
)
public class UserServiceLeastActiveImpl implements UserService {

    /**
     * 当前活跃请求数计数器（线程安全）
     * 
     * <p>使用 AtomicInteger 而非 int 或 Integer 的原因：</p>
     * <ul>
     *   <li><b>原子性</b>：incrementAndGet() 和 decrementAndGet() 是原子操作，
     *       不会出现竞态条件（Race Condition）</li>
     *   <li><b>无锁</b>：基于 CAS（Compare And Swap）实现，性能优于 synchronized</li>
     *   <li><b>可见性</b>：volatile 语义保证多线程间的数据一致性</li>
     * </ul>
     * 
     * <p><b>生命周期管理：</b><br>
     * 进入方法时 incrementAndGet()（活跃数+1）<br>
     * 在 finally 块中 decrementAndGet()（活跃数-1），确保即使发生异常也能正确释放<br>
     * 这种模式类似于数据库连接池的"借用-归还"机制
     */
    private final AtomicInteger activeCount = new AtomicInteger(0);

    /**
     * 用户登录方法 - 使用最少活跃数负载均衡策略
     * 
     * <p><b>执行流程：</b></p>
     * <ol>
     *   <li><b>增加活跃计数</b>：activeCount.incrementAndGet()
 *       <ul>
     *         <li>将当前活跃请求数+1</li>
     *         <li>告知 Dubbo 框架："我开始处理了"</li>
     *         <li>这会影响后续请求的路由决策</li>
     *       </ul>
     *   </li>
     *   <li><b>执行业务逻辑</b>：simulateProcessing(40)
     *       <ul>
     *         <li>模拟40ms的处理时间（最短，代表高性能节点）</li>
     *         <li>实际项目中可能是：DB查询、RPC调用、缓存读取等</li>
     *       </ul>
     *   </li>
     *   <li><b>构建返回结果</b>：包含当前活跃数信息
     *       <ul>
     *         <li>方便观察者理解当前节点的负载状态</li>
     *         <li>[LEASTACTIVE] 前缀标识策略类型</li>
     *       </ul>
     *   </li>
     *   <li><b>减少活跃计数</b>（在 finally 中）：activeCount.decrementAndGet()
     *       <ul>
     *         <li>无论正常还是异常，都必须执行</li>
     *         <li>防止资源泄漏导致活跃数只增不减</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 包含策略标识、用户名、当前活跃数、权重的结果
     *         示例: "[LEASTACTIVE] admin 登录成功 (当前活跃=3, 权重=60)"
     */
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
