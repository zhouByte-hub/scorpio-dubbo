package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 超时与重试机制测试控制器
 * 
 * <h2>功能说明</h2>
 * 该控制器演示了 Dubbo RPC 调用中的<strong>超时控制</strong>和<strong>重试机制</strong>。
 * 这是分布式系统中保障服务稳定性的重要手段。
 * 
 * <h2>为什么需要超时机制？</h2>
 * <p>在分布式系统中，RPC 调用可能会因为以下原因长时间阻塞：</p>
 * <ul>
 *   <li>Provider 处理逻辑复杂（如大数据查询）</li>
 *   <li>数据库查询慢（缺少索引、数据量大）</li>
 *   <li>网络拥塞或丢包</li>
 *   <li>Provider 发生 GC 停顿（Stop-The-World）</li>
 *   <li>死锁或资源竞争导致线程阻塞</li>
 * </ul>
 * <p>如果没有超时限制，调用方的线程会被永久阻塞，最终耗尽线程池资源。</p>
 * 
 * <h2>Dubbo 超时配置层级</h2>
 * <p>Dubbo 支持多级超时配置，优先级从高到低：</p>
 * <ol>
 *   <li><b>方法级</b>: @DubboReference(methods = {@Method(name="xxx", timeout=5000)})</li>
 *   <li><b>接口级（本示例）</b>: @DubboReference(timeout=3000)</li>
 *   <li><b>全局级</b>: dubbo.consumer.timeout=3000（application.yaml）</li>
 *   <li><b>默认值</b>: Dubbo 默认超时时间为 1000ms（1秒）</li>
 * </ol>
 * 
 * <h2>重试机制的利弊</h2>
 * <table border="1">
 *   <tr>
 *     <th>优点</th>
 *     <th>缺点</th>
 *   </tr>
 *   <tr>
 *     <td>提高成功率（应对瞬时故障）</td>
 *     <td>可能导致请求放大（重试风暴）</td>
 *   </tr>
 *   <tr>
 *     <td>用户无感知（自动恢复）</td>
 *     <td>不适合写操作（可能导致重复提交）</td>
 *   </tr>
 *   <tr>
 *     <td>简单易用（无需额外代码）</td>
 *     <td>增加系统负载（多次RPC调用）</td>
 *   </tr>
 * </table>
 * 
 * <h2>最佳实践建议</h2>
 * <ul>
 *   <li>读操作：启用重试（retries=2~3），配合 Failover 模式</li>
 *   <li>写操作：禁用重试（retries=0），使用 Failfast 模式</li>
 *   <li>超时时间：根据业务 P99 响应时间的 2-3 倍设置</li>
 *   <li>监控告警：关注超时率指标，超过阈值及时告警</li>
 * </ul>
 * 
 * @author zhouByte
 * @version 1.0.0
 */
@RestController
@RequestMapping("/timeout")
public class TimeoutRetryController {

    /**
     * 正常超时配置的服务引用
     * 
     * <p><b>配置特点：</b></p>
     * <ul>
     *   <li>timeout=3000（3秒）- 较宽松的超时设置，允许较慢的处理</li>
     *   <li>retries=2（重试2次）- 允许在失败时自动重试</li>
     *   <li>总最大耗时 = timeout × (retries+1) = 3s × 3 = 9秒</li>
     * </ul>
     * 
     * <p><b>适用场景：</b></p>
     * <ul>
     *   <li>非实时性要求的后台任务</li>
     *   <li>允许一定延迟的报表查询</li>
     *   <li>对成功率要求较高的读操作</li>
     * </ul>
     * 
     * <p><b>性能影响：</b></p>
     * 最坏情况下，一次调用可能消耗9秒（3秒×3次尝试），需要评估是否满足SLA要求。
     */
    @DubboReference(
            interfaceClass = UserService.class,
            timeout = 3000,
            retries = 2
    )
    private UserService normalTimeoutService;

    /**
     * 严格超时配置的服务引用（不重试）
     * 
     * <p><b>配置特点：</b></p>
     * <ul>
     *   <li>timeout=1000（1秒）- 较严格的超时设置，要求快速响应</li>
     *   <li>retries=0（不重试）- 失败立即返回，不做任何补偿</li>
     *   <li>总最大耗时 = timeout × 1 = 1秒（可控且快速）</li>
     * </ul>
     * 
     * <p><b>适用场景：</b></p>
     * <ul>
     *   <li>实时性要求高的接口（如支付回调）</li>
     *   <li>写操作（避免重复提交的风险）</li>
     *   <li>前端用户直接等待的场景（用户体验敏感）</li>
     *   <li>熔断降级后的兜底调用（快速失败，触发降级逻辑）</li>
     * </ul>
     * 
     * <p><b>⚠️ 注意事项：</b></p>
     * 1秒的超时对于复杂业务可能过于紧张，
     * 需要确保 Provider 端能在1秒内完成处理（P99 < 800ms）。
     */
    @DubboReference(
            interfaceClass = UserService.class,
            timeout = 1000,
            retries = 0
    )
    private UserService shortTimeoutNoRetryService;

    /**
     * 测试正常超时配置（3秒超时 + 允许重试2次）
     * 
     * <p><b>接口功能：</b></p>
     * 使用宽松的超时配置调用远程服务，记录并返回实际的耗时信息。
     * 
     * <p><b>执行流程：</b></p>
     * <ol>
     *   <li>记录开始时间戳（System.currentTimeMillis()）</li>
     *   <li>发起 RPC 调用（可能在内部重试0-2次）</li>
     *   <li>记录结束时间戳，计算总耗时</li>
     *   <li>构建包含结果和统计信息的返回字符串</li>
     * </ol>
     * 
     * <p><b>返回值格式（成功）：</b></p>
     * <pre>
     * {登录结果}
     * 耗时: 156ms
     * 配置: timeout=3000ms, retries=2
     * </pre>
     * 
     * <p><b>返回值格式（超时）：</b></p>
     * <pre>
     * [TIMEOUT] 调用超时: ...
     * 耗时: 3050ms
     * </pre>
     * 
     * <p><b>观察要点：</b></p>
     * <ul>
     *   <li>正常情况下耗时应该在50-200ms左右（Provider模拟耗时）</li>
     *   <li>如果超过3秒，说明发生了超时异常</li>
     *   <li>如果发生重试，总耗时可能是3秒的整数倍</li>
     * </ul>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 包含调用结果、实际耗时、配置信息的字符串
     *         HTTP GET /timeout/normal/admin/123456
     */
    @GetMapping("/normal/{username}/{password}")
    public String testNormalTimeout(@PathVariable String username, @PathVariable String password) {
        long start = System.currentTimeMillis();
        try {
            String result = normalTimeoutService.userLogin(username, password);
            long cost = System.currentTimeMillis() - start;
            return result + "\n耗时: " + cost + "ms\n配置: timeout=3000ms, retries=2";
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            return "[TIMEOUT] 调用超时: " + e.getMessage() + "\n耗时: " + cost + "ms";
        }
    }

    /**
     * 测试严格超时配置（1秒超时 + 不重试）
     * 
     * <p><b>接口功能：</b></p>
     * 使用严格的超时配置调用远程服务，强调快速失败的原则。
     * 
     * <p><b>与 normal 接口的区别：</b></p>
     * <table border="1">
     *   <tr>
     *     <th>特性</th>
     *     <th>/normal</th>
     *     <th>/strict</th>
     *   </tr>
     *   <tr>
     *     <td>超时时间</td>
     *     <td>3秒</td>
     *     <td>1秒 ⚡</td>
     *   </tr>
     *   <tr>
     *     <td>重试次数</td>
     *     <td>2次</td>
     *     <td>0次 ❌</td>
     *   </tr>
     *   <tr>
     *     <td>最大耗时</td>
     *     <td>9秒</td>
     *     <td>1秒 ✅</td>
     *   </tr>
     *   <tr>
     *     <td>适用场景</td>
     *     <td>后台任务</td>
     *     <td>实时交互</td>
     *   </tr>
     * </table>
     * 
     * <p><b>返回值格式（成功）：</b></p>
     * <pre>
     * {登录结果}
     * 耗时: 52ms
     * 配置: timeout=1000ms, retries=0(不重试)
     * </pre>
     * 
     * <p><b>返回值格式（超时）：</b></p>
     * <pre>
     * [TIMEOUT] 调用超时: ...
     * 耗时: 1020ms
     * 严格模式：超时立即报错，不重试
     * </pre>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 包含调用结果、耗时、严格模式说明的字符串
     *         HTTP GET /timeout/strict/admin/123456
     */
    @GetMapping("/strict/{username}/{password}")
    public String testStrictTimeout(@PathVariable String username, @PathVariable String password) {
        long start = System.currentTimeMillis();
        try {
            String result = shortTimeoutNoRetryService.userLogin(username, password);
            long cost = System.currentTimeMillis() - start;
            return result + "\n耗时: " + cost + "ms\n配置: timeout=1000ms, retries=0(不重试)";
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            return "[TIMEOUT] 调用超时: " + e.getMessage() + "\n耗时: " + cost + "ms\n严格模式：超时立即报错，不重试";
        }
    }
}
