package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集群容错模式测试控制器
 * 
 * <h2>功能说明</h2>
 * 该控制器演示了 Dubbo 的3种主要集群容错（Cluster）模式：
 * <ul>
 *   <li><b>Failover（失败自动切换）</b> - 默认模式，失败后重试其他节点</li>
 *   <li><b>Failfast（快速失败）</b> - 只尝试一次，失败立即报错</li>
 *   <li><b>Failsafe（安全失败）</b> - 异常被忽略，返回null或空值</li>
 * </ul>
 * 
 * <h2>什么是集群容错？</h2>
 * <p>当 Consumer 调用远程服务时，可能会遇到各种异常情况：
 * <ul>
 *   <li>网络抖动导致连接超时</li>
 *   <li>Provider 服务宕机或未启动</li>
 *   <li>业务逻辑抛出异常</li>
 *   <li>Provider 响应过慢</li>
 * </ul>
 * 集群容错机制决定了在出现这些异常时，Consumer 应该如何处理。</p>
 * 
 * <h2>Dubbo Cluster 模式对比</h2>
 * <table border="1">
 *   <tr>
 *     <th>模式</th>
 *     <th>失败处理</th>
 *     <th>重试次数</th>
 *     <th>适用场景</th>
 *   </tr>
 *   <tr>
 *     <td>Failover</td>
 *     <td>自动切换到其他节点</td>
 *     <td>可配置（默认2次）</td>
 *     <td>读操作、幂等操作</td>
 *   </tr>
 *   <tr>
 *     <td>Failfast</td>
 *     <td>立即抛出异常</td>
 *     <td>不重试（0次）</td>
 *     <td>写操作、非幂等操作</td>
 *   </tr>
 *   <tr>
 *     <td>Failsafe</td>
 *     <td>忽略异常，返回null</td>
 *     <td>不重试</td>
 *     <td>日志记录、审计、非核心流程</td>
 *   </tr>
 * </table>
 * 
 * <h2>@DubboReference 关键配置项</h2>
 * <ul>
 *   <li><b>cluster</b>: 集群容错模式名称</li>
 *   <li><b>retries</b>: 失败后的重试次数（仅 Failover 模式生效）</li>
 *   <li><b>timeout</b>: 单次调用的超时时间（毫秒）</li>
 * </ul>
 * 
 * @author zhouByte
 * @version 1.0.0
 */
@RestController
@RequestMapping("/cluster")
public class ClusterFaultToleranceController {

    /**
     * Failover（失败自动切换）模式的服务引用
     * 
     * <p><b>工作原理：</b></p>
     * 当调用失败时，Dubbo 会自动重试其他可用的 Provider 节点，
     * 直到成功或达到最大重试次数（retries=3）为止。
     * 
     * <p><b>执行流程示例：</b></p>
     * <pre>
     * 第1次调用 → Provider-A → 失败（超时/异常）
     * 第2次调用 → Provider-B → 成功 ✅ （返回结果）
     * 
     * 如果所有重试都失败：
     * 第1次 → A（失败）
     * 第2次 → B（失败）
     * 第3次 → C（失败）
     * 第4次 → 抛出最后一次的异常给 Consumer
     * </pre>
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li>cluster="failover" → 启用失败自动切换模式</li>
     *   <li>retries=3 → 最多重试3次（加上首次调用，总共最多4次尝试）</li>
     *   <li>timeout=2000 → 每次调用等待不超过2秒</li>
     * </ul>
     * 
     * <p><b>⚠️ 注意事项：</b></p>
     * Failover 模式只适合<strong>幂等操作</strong>（多次执行结果相同），如查询操作。
     * 对于写操作（如新增订单），可能导致重复插入！
     */
    @DubboReference(
            interfaceClass = UserService.class,
            cluster = "failover",
            retries = 3,
            timeout = 2000
    )
    private UserService failoverService;

    /**
     * Failfast（快速失败）模式的服务引用
     * 
     * <p><b>工作原理：</b></p>
     * 只发起一次RPC调用，无论成功还是失败都立即返回。
     * 不进行任何重试，将异常直接抛给调用方处理。
     * 
     * <p><b>适用场景：</b></p>
     * <ul>
     *   <li><strong>写操作</strong> - 如新增订单、扣减库存（非幂等，不能重复执行）</li>
     *   <li><strong>需要原样异常</strong> - 调用方需要知道具体的错误原因</li>
     *   <li><strong>实时性要求高</strong> - 不能容忍重试带来的延迟</li>
     * </ul>
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li>cluster="failfast" → 快速失败模式</li>
     *   <li>timeout=2000 → 单次调用超时2秒</li>
     *   <li>未设置 retries → 默认不重试（即使设置了也不生效）</li>
     * </ul>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            cluster = "failfast",
            timeout = 2000
    )
    private UserService failfastService;

    /**
     * Failsafe（安全失败）模式的服务引用
     * 
     * <p><b>工作原理：</b></p>
     * 调用失败时，<strong>不会抛出异常</strong>，而是：
     * <ul>
     *   <li>返回 null（对于对象类型）</li>
     *   <li>返回 false/0（对于基本类型）</li>
     *   <li>返回空集合（对于集合类型）</li>
     * </ul>
     * 异常信息会被记录到日志中，但对调用方透明。
     * 
     * <p><b>适用场景：</b></p>
     * <ul>
     *   <li><strong>日志记录</strong> - 操作失败不影响主流程</li>
     *   <li><strong>审计追踪</strong> - 记录用户行为但不强制要求成功</li>
     *   <li><strong>非核心功能</strong> - 如发送通知、更新统计数据</li>
     *   <li><strong>降级兜底</strong> - 作为熔断降级的最后手段</li>
     * </ul>
     * 
     * <p><b>⚠️ 风险提示：</b></p>
     * 使用 Failsafe 时，调用方必须做好 null 值检查，
     * 否则可能出现 NullPointerException！
     */
    @DubboReference(
            interfaceClass = UserService.class,
            cluster = "failsafe",
            timeout = 2000
    )
    private UserService failsafeService;

    /**
     * 测试 Failover（失败自动切换）容错模式
     * 
     * <p><b>接口说明：</b></p>
     * 调用配置为 failover 模式的服务，观察其容错行为。
     * 在正常情况下，应该能成功返回结果；
     * 如果某个 Provider 故障，会自动切换到其他可用节点。
     * 
     * <p><b>返回值格式：</b></p>
     * [FAILOVER] {登录结果}\n失败自动切换，重试次数=3
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 登录结果 + 模式说明
     *         HTTP GET /cluster/failover/admin/123456
     */
    @GetMapping("/failover/{username}/{password}")
    public String testFailover(@PathVariable String username, @PathVariable String password) {
        return "[FAILOVER] " + failoverService.userLogin(username, password)
                + "\n失败自动切换，重试次数=3";
    }

    /**
     * 测试 Failfast（快速失败）容错模式
     * 
     * <p><b>接口说明：</b></p>
     * 调用配置为 failfast 模式的服务。
     * 如果发生异常，会立即被捕获并返回错误信息，不会进行重试。
     * 
     * <p><b>异常处理：</b></p>
     * 使用 try-catch 捕获可能的 RpcException 或 TimeoutException，
     * 将异常信息格式化后返回给前端展示。
     * 
     * <p><b>返回值示例（成功）：</b></p>
     * [FAILFAST] admin 登录成功\n快速失败模式：只尝试1次，失败立即报错
     * 
     * <p><b>返回值示例（失败）：</b></p>
     * [FAILFAST] 调用失败: RpcException: ...\n快速失败模式：只尝试1次，失败立即报错
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 登录结果或错误信息
     *         HTTP GET /cluster/failfast/admin/123456
     */
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

    /**
     * 测试 Failsafe（安全失败）容错模式
     * 
     * <p><b>接口说明：</b></p>
     * 调用配置为 failsafe 模式的服务。
     * 即使服务端抛出异常，Consumer 端也不会收到异常，
     * 而是会得到一个 null 或空值。
     * 
     * <p><b>⚠️ 重要提示：</b></p>
     * 在实际业务中，使用 Failsafe 模式时必须对返回值进行 null 检查，
     * 以避免后续代码出现 NullPointerException。
     * 
     * <p><b>返回值示例：</b></p>
     * [FAILSAFE] admin 登录成功\n安全失败模式：异常被忽略，不会抛出
     * 或
     * [FAILSAFE] 调用结果: null\n安全失败模式：异常被忽略，不会抛出
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 登录结果或 null 提示信息
     *         HTTP GET /cluster/failsafe/admin/123456
     */
    @GetMapping("/failsafe/{username}/{password}")
    public String testFailsafe(@PathVariable String username, @PathVariable String password) {
        String result = failsafeService.userLogin(username, password);
        return "[FAILSAFE] 调用结果: " + result
                + "\n安全失败模式：异常被忽略，不会抛出";
    }

    /**
     * 对比三种容错模式的差异
     * 
     * <p><b>接口功能：</b></p>
     * 同时调用三种容错模式的服务，将结果汇总对比，
     * 直观展示不同模式在相同条件下的表现差异。
     * 
     * <p><b>输出内容：</b></p>
     * <ol>
     *   <li>Failover 模式的结果（可能经过重试）</li>
     *   <li>Failfast 模式的结果（单次尝试）</li>
     *   <li>Failsafe 模式的结果（可能为null）</li>
     *   <li>各模式的简要说明</li>
     * </ol>
     * 
     * <p><b>学习价值：</b></p>
     * 通过这个接口可以一次性了解三种模式的特点，
     * 便于在实际项目中根据业务需求选择合适的容错策略。
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 三种模式的对比结果
     *         HTTP GET /cluster/compare/admin/123456
     */
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
