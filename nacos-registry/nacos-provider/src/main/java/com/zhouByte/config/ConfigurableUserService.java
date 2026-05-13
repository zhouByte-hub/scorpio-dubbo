package com.zhouByte.config;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 可动态配置的用户服务实现类
 * 
 * <h2>功能说明</h2>
 * 该类演示了 Dubbo 的<strong>动态配置</strong>能力，
 * 允许在运行时通过配置中心（如 Nacos）修改服务参数，而无需重启应用。
 * 
 * <h2>Dubbo 动态配置机制</h2>
 * <p>Dubbo 支持多层次的配置体系，优先级从高到低：</p>
 * <ol>
 *   <li><b>JVM -D 参数</b>: 启动时传入，最高优先级</li>
 *   <li><b>方法级 @Method 注解</b>: 针对单个方法的精细控制</li>
 *   <li><b>接口级 @DubboService/@DubboReference</b>: 针对整个服务的配置</li>
 *   <li><b>全局配置（application.yaml）</b>: 应用于所有服务</li>
 *   <li><b>注册中心动态配置</b>: 运行时可通过 Nacos/Zookeeper 修改 ✨</li>
 * </ol>
 * 
 * <h2>@DubboService 配置详解</h2>
 * <pre>
 * @DubboService(
 *     interfaceClass = UserService.class,  // 服务接口
 *     group = "dynamic-config",            // 分组标识
 *     version = "1.0.0",                   // 版本号
 *     methods = {                          // 方法级配置数组
 *         @Method(
 *             name = "userLogin",           // 方法名
 *             timeout = 3000,              // 超时时间（毫秒）
 *             retries = 2,                 // 重试次数
 *             loadbalance = "random"       // 负载均衡策略
 *         )
 *     }
 * )
 * </pre>
 * 
 * <h2>可动态调整的参数列表</h2>
 * <table border="1">
 *   <tr>
 *     <th>参数名</th>
 *     <th>类型</th>
 *     <th>默认值</th>
 *     <th>说明</th>
 *   </tr>
 *   <tr>
 *     <td>timeout</td>
 *     <td>int (ms)</td>
 *     <td>1000</td>
 *     <td>RPC调用超时时间</td>
 *   </tr>
 *   <tr>
 *     <td>retries</td>
 *     <td>int</td>
 *     <td>2（Failover模式）</td>
 *     <td>失败重试次数</td>
 *   </tr>
 *   <tr>
 *     <td>loadbalance</td>
 *     <td>String</td>
 *     <td>random</td>
 *     <td>负载均衡策略</td>
 *   </tr>
 *   <tr>
 *     <td>weight</td>
 *     <td>int</td>
 *     <td>100</td>
 *     <td>节点权重（0-1000）</td>
 *   </tr>
 *   <tr>
 *     <td>connections</td>
 *     <td>int</td>
 *     <td>0（共享连接）</td>
 *     <td>最大连接数</td>
 *   </tr>
 *   <tr>
 *     <td>async</td>
 *     <td>boolean</td>
 *     <td>false</td>
 *     <td>是否异步调用</td>
 *   </tr>
 *   <tr>
 *     <td>actives</td>
 *     <td>int</td>
 *     <td>0（不限流）</td>
 *     <td>最大并发数</td>
 *   </tr>
 * </table>
 * 
 * <h2>Nacos 动态配置示例</h2>
 * <p>在 Nacos 控制台中添加如下配置，即可实时生效：</p>
 * <pre>
 * Data ID: dubbo-provider-config.properties
 * Group: DEFAULT_GROUP
 * 
 * Content:
 * # 修改超时时间为5秒（原值3秒）
 * dubbo.service.parameters.timeout=5000
 * 
 * # 修改重试次数为3次（原值2次）
 * dubbo.service.parameters.retries=3
 * 
 * # 切换负载均衡策略为轮询（原值随机）
 * dubbo.service.parameters.loadbalance=roundrobin
 * 
 * # 降低该节点的权重至50（原值100）
 * dubbo.service.parameters.weight=50
 * </pre>
 * 
 * <h2>适用场景</h2>
 * <ul>
 *   <li><b>🔧 故障排查</b>: 临时调大超时时间定位慢请求问题</li>
 *   <li><b>📊 性能优化</b>: 根据监控数据动态调整参数</li>
 *   <li><b>🚀 灰度发布</b>: 逐步调整权重，平滑迁移流量</li>
 *   <li><b>🛡️ 容灾切换</b>: 在故障时快速降级或限流</li>
 *   <li><b>🎯 A/B 测试</b>: 对不同用户群体应用不同的配置</li>
 * </ul>
 * 
 * <h2>线程安全说明</h2>
 * 使用 {@link AtomicLong} 统计调用次数，保证在多线程环境下的计数准确性。
 * 
 * @author zhouByte
 * @version 1.0.0
 * @see UserService
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "dynamic-config",
        version = "1.0.0",
        methods = {
                @Method(
                        name = "userLogin",
                        timeout = 3000,
                        retries = 2,
                        loadbalance = "random"
                )
        }
)
public class ConfigurableUserService implements UserService {

    /**
     * 原子计数器 - 用于统计总调用次数
     * 
     * <p><b>为什么使用 AtomicLong？</b></p>
     * <ul>
     *   <li><b>线程安全</b>: incrementAndGet() 是原子操作，无需加锁</li>
     *   <li><b>高性能</b>: 基于 CAS 实现，比 synchronized 更轻量</li>
     *   <li><b>可见性</b>: volatile 语义保证其他线程能立即看到最新值</li>
     * </ul>
     * 
     * <p><b>应用场景：</b></p>
     * 用于演示动态配置的效果——每次调用都会返回当前的调用序号，
     * 结合返回的配置信息，可以直观地看到配置变更的影响。
     */
    private final AtomicLong callCount = new AtomicLong(0);

    /**
     * 用户登录方法 - 支持动态配置的版本
     * 
     * <p><b>方法功能：</b></p>
     * <ol>
     *   <li><b>统计调用次数</b>: 使用原子计数器记录这是第几次调用</li>
     *   <li><b>构建响应信息</b>: 包含当前服务的详细配置信息</li>
     *   <li><b>展示可配置项</b>: 列出所有可以通过 Nacos 动态修改的参数</li>
     * </ol>
     * 
     * <p><b>返回值格式：</b></p>
     * <pre>
     * [动态配置服务] 第 N 次调用
     * 用户: admin
     * 
     * 当前服务配置信息:
     * - Group: dynamic-config
     * - Version: 1.0.0
     * - 可通过 Nacos 动态修改:
     *   * timeout (超时时间)
     *   * retries (重试次数)
     *   * loadbalance (负载均衡策略)
     *   * weight (权重)
     * </pre>
     * 
     * <p><b>如何验证动态配置？</b></p>
     * <ol>
     *   <li>启动 Provider 和 Consumer 应用</li>
     *   <li>访问此接口多次，观察默认行为</li>
     *   <li>打开 Nacos 控制台 → 配置管理 → 新建配置</li>
     *   <li>修改 timeout 参数（如从3000改为8000）</li>
     *   <li>再次访问接口，观察超时行为的变化</li>
     *   <li>无需重启任何应用！配置会自动生效 ✅</li>
     * </ol>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 包含调用次数、用户信息和当前配置详情的结果字符串
     */
    @Override
    public String userLogin(String username, String password) {
        long currentCall = callCount.incrementAndGet();
        
        StringBuilder result = new StringBuilder();
        result.append("[动态配置服务] 第 ").append(currentCall).append(" 次调用\n");
        result.append("用户: ").append(username).append("\n");
        result.append("\n当前服务配置信息:\n");
        result.append("- Group: dynamic-config\n");
        result.append("- Version: 1.0.0\n");
        result.append("- 可通过 Nacos 动态修改:\n");
        result.append("  * timeout (超时时间)\n");
        result.append("  * retries (重试次数)\n");
        result.append("  * loadbalance (负载均衡策略)\n");
        result.append("  * weight (权重)");
        
        return result.toString();
    }
}
