package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 随机负载均衡策略实现类
 * 
 * <h2>功能说明</h2>
 * 该类实现了 UserService 接口，使用 Dubbo 的随机（Random）负载均衡策略。
 * 随机策略会按照权重随机选择一个服务提供者进行调用。
 * 
 * <h2>Dubbo 配置说明</h2>
 * <ul>
 *   <li><b>interfaceClass</b>: 指定服务接口类型，Dubbo 根据此接口生成代理对象</li>
 *   <li><b>group</b>: 服务分组，用于逻辑隔离不同的服务实例。
 *       此处设置为 "balance-random"，表示该服务属于"负载均衡-随机"分组，
 *       Consumer 端通过 group 参数来定位到该服务的具体实现。</li>
 *   <li><b>weight</b>: 权重值，范围 0-1000，默认为100。
 *       权重越高，被选中的概率越大。
 *       例如：如果 A 权重=100, B 权重=200，那么 B 被选中的概率是 A 的2倍。
 *       此处设置权重=100，作为基准值。</li>
 * </ul>
 * 
 * <h2>使用场景</h2>
 * <ul>
 *   <li>适用于各服务器性能相近的场景</li>
 *   <li>请求量分布均匀的场景</li>
 *   <li>简单的负载均衡需求</li>
 * </ul>
 * 
 * <h2>测试方式</h2>
 * Consumer 端配置 loadbalance="random"，多次调用观察返回结果是否均匀分布
 * 
 * @author zhouByte
 * @version 1.0.0
 * @since 2024-01-01
 * @see UserService
 * @see org.apache.dubbo.rpc.cluster.loadbalance.RandomLoadBalance
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "balance-random",
        weight = 100
)
public class UserServiceRandomImpl implements UserService {

    /**
     * 用户登录方法 - 使用随机负载均衡策略处理
     * 
     * <p>该方法模拟了一个用户登录的业务场景：
     * <ol>
     *   <li>接收用户名和密码参数</li>
     *   <li>模拟业务处理耗时（50ms），模拟数据库查询、密码校验等操作</li>
     *   <li>返回包含策略标识的登录结果字符串</li>
     * </ol>
     * 
     * <p><b>返回值格式说明：</b><br>
     * [RANDOM] {username} 登录成功 (权重=100)<br>
     * 其中 [RANDOM] 前缀用于标识当前使用的是随机策略
     * 
     * @param username 用户名，用于登录认证的用户标识
     *                示例值: "admin", "user001", "zhangsan"
     * @param password 用户密码，用于身份验证
     *                注意：实际生产环境中应该使用加密传输和存储
     * @return String 登录结果信息，包含策略标识、用户名、权重信息
     *         返回示例: "[RANDOM] admin 登录成功 (权重=100)"
     */
    @Override
    public String userLogin(String username, String password) {
        simulateProcessing(50);
        return "[RANDOM] " + username + " 登录成功 (权重=100)";
    }

    /**
     * 模拟业务处理耗时方法
     * 
     * <p>该方法用于模拟真实的业务处理时间，例如：
     * <ul>
     *   <li>数据库查询操作</li>
     *   <li>密码加密验证</li>
     *   <li>用户权限校验</li>
     *   <li>日志记录等辅助操作</li>
     * </ul>
     * 
     * <p><b>为什么需要模拟耗时？</b><br>
     * 在演示负载均衡效果时，如果处理速度过快，很难观察到不同策略的差异。
     * 通过添加适当的延迟，可以更清晰地看到：
     * <ul>
     *   <li>并发场景下各节点的压力分布</li>
     *   <li>不同策略的选择行为差异</li>
     *   <li>响应时间的统计对比</li>
     * </ul>
     * 
     * @param ms 模拟耗时的毫秒数，建议值：10-200ms
     *            过短难以观察效果，过长影响用户体验
     */
    private void simulateProcessing(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
