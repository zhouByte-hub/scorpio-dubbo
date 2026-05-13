package com.zhouByte.governance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务治理能力测试控制器
 * 
 * <h2>功能说明</h2>
 * 该控制器全面演示了 Dubbo 的<strong>服务治理</strong>核心能力，
 * 包括版本管理、标签路由、条件路由、分组路由等高级特性。
 * 
 * <h2>什么是服务治理？</h2>
 * <p>服务治理是指在微服务架构中，对服务的<strong>生命周期</strong>、<strong>流量分配</strong>、
 * <strong>服务质量</strong>等进行管理和控制的机制。主要包括：</p>
 * <ul>
 *   <li><b>服务发现与注册</b> - Provider 上线/下线感知</li>
 *   <li><b>负载均衡</b> - 请求分发策略</li>
 *   <li><b>容错处理</b> - 失败重试、熔断降级</li>
 *   <li><b>流量控制</b> - 版本路由、标签路由、条件路由</li>
 *   <li><b>配置管理</b> - 动态调整参数</li>
 *   <li><b>监控告警</b> - 性能指标采集与分析</li>
 * </ul>
 * 
 * <h2>本控制器涵盖的功能模块</h2>
 * <ol>
 *   <li><b>📌 版本路由（Version Routing）</b>
 *       <ul>
 *         <li>精确匹配特定版本（如 V1.0.0、V2.0.0）</li>
 *         <li>通配符匹配任意版本（version="*"）</li>
 *         <li>支持灰度发布、金丝雀部署</li>
 *       </ul>
 *   </li>
 *   <li><b>🏷️ 标签路由（Tag Router）</b>
 *       <ul>
 *         <li>基于请求标签将流量路由到特定的 Provider 子集</li>
 *         <li>典型场景：金丝雀发布、A/B 测试、环境隔离</li>
 *         <li>需要配合 Nacos 配置中心使用</li>
 *       </ul>
 *   </li>
 *   <li><b>🔀 条件路由（Condition Router）</b>
 *       <ul>
 *         <li>根据消费者/提供者的元数据进行路由决策</li>
 *         <li>支持复杂的布尔表达式组合</li>
 *         <li>典型场景：黑白名单、机房隔离、读写分离</li>
 *       </ul>
 *   </li>
 *   <li><b>📦 分组路由（Group Routing）</b>
 *       <ul>
 *         <li>按业务模块或租户进行逻辑隔离</li>
 *         <li>同一接口可以有多个不同的实现组</li>
 *         <li>Consumer 通过 group 参数定位到目标组</li>
 *       </ul>
 *   </li>
 * </ol>
 * 
 * <h2>REST API 接口列表</h2>
 * <table border="1">
 *   <tr>
 *     <th>HTTP 方法</th>
 *     <th>路径</th>
 *     <th>功能</th>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/governance/version/v1/{user}/{pwd}</td>
 *     <td>调用 V1.0.0 版本服务</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/governance/version/v2/{user}/{pwd}</td>
 *     <td>调用 V2.0.0 版本服务</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/governance/version/any/{user}/{pwd}</td>
 *     <td>通配符匹配任意版本</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/governance/tag/canary/{user}/{pwd}</td>
 *     <td>标签路由（金丝雀发布）</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/governance/condition/{user}/{pwd}</td>
 *     <td>条件路由演示</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/governance/group/demo/{user}/{pwd}</td>
 *     <td>分组路由演示</td>
 *   </tr>
 * </table>
 * 
 * <h2>前置条件</h2>
 * <ul>
 *   <li>Nacos 注册中心已启动并正常运行</li>
 *   <li>nacos-provider 应用已启动（包含 V1 和 V2 版本的 UserService 实现）</li>
 *   <li>nacos-consumer 应用已启动（即当前应用）</li>
 * </ul>
 * 
 * @author zhouByte
 * @version 1.0.0
 * @see UserService
 * @see UserServiceV1
 * @see UserServiceV2
 */
@RestController
@RequestMapping("/governance")
public class GovernanceController {

    /**
     * 引用 V1.0.0 版本的 UserService
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li>version="1.0.0" → 精确匹配版本号为 1.0.0 的 Provider</li>
     *   <li>group="governance-version" → 指定服务分组</li>
     * </ul>
     * 
     * <p><b>路由逻辑：</b><br>
     * 当 Consumer 发起 RPC 调用时，Dubbo 会：</p>
     * <ol>
     *   <li>查找所有实现了 UserService 接口的 Provider</li>
     *   <li>过滤出 group="governance-version" 的实例</li>
     *   <li>再过滤出 version="1.0.0" 的实例</li>
     *   <li>从候选列表中选择一个进行调用（按负载均衡策略）</li>
     * </ol>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            version = "1.0.0",
            group = "governance-version"
    )
    private UserService v1UserService;

    /**
     * 引用 V2.0.0 版本的 UserService
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li>version="2.0.0" → 匹配最新版本的 Provider</li>
     *   <li>group="governance-version" → 与 V1 使用相同的分组</li>
     * </ul>
     * 
     * <p><b>使用场景：</b><br>
     * 当需要使用新功能（如 BCrypt 加密、MFA 认证）时，
     * 可以通过此引用直接调用 V2 版本的服务实现。</p>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            version = "2.0.0",
            group = "governance-version"
    )
    private UserService v2UserService;

    /**
     * 通配符引用 - 可匹配任意版本的 UserService
     * 
     * <p><b>特殊配置：</b></p>
     * <ul>
     *   <li>version="*" → 通配符，表示接受任何版本</li>
     *   <li>当存在多个版本时，Dubbo 会随机选择其中一个</li>
     * </ul>
     * 
     * <p><b>⚠️ 注意事项：</b></p>
     * <ul>
     *   <li>在生产环境中慎用通配符，可能导致不可预测的行为</li>
     *   <li>更适合用于测试、调试或作为降级方案</li>
     *   <li>如果需要明确控制版本流向，请使用具体的版本号</li>
     * </ul>
     * 
     * <p><b>典型用法：</b></p>
     * <pre>
     * // 场景1：开发测试阶段，快速验证功能
     * // 场景2：作为默认回退选项（当特定版本不可用时）
     * // 场景3：配合标签路由一起使用（由标签决定最终目标）
     * </pre>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            version = "*",
            group = "governance-version"
    )
    private UserService anyVersionUserService;

    /**
     * 测试版本路由 - 调用 V1.0.0 版本
     * 
     * <p><b>接口功能：</b></p>
     * 通过显式指定 version="1.0.0"，确保请求只被路由到 V1 版本的 Provider。
     * 这是版本路由的最基本用法——<strong>精确匹配</strong>。
     * 
     * <p><b>预期结果：</b></p>
     * 返回的内容中应该包含 "[V1.0.0]" 前缀，
     * 表明确实是由 V1 版本的服务处理的请求。
     * 
     * <p><b>应用场景示例：</b></p>
     * <pre>
     * 假设你的系统正在从 V1 升级到 V2：
     * 
     * 1. 部分老客户还在使用旧的 API 客户端（只认识 V1）
     *    → 这些请求应该继续走 V1，避免兼容性问题
     * 
     * 2. 新客户已经更新了客户端（支持 V2）
     *    → 这些请求应该走 V2，享受新功能
     * 
     * 3. 通过版本路由，可以实现平滑过渡，无需停机维护
     * </pre>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String V1 版本的登录结果
     *         HTTP GET /governance/version/v1/admin/123456
     */
    @GetMapping("/version/v1/{username}/{password}")
    public String testVersionV1(@PathVariable String username, @PathVariable String password) {
        return "[版本路由] 调用 V1.0.0 服务:\n" + v1UserService.userLogin(username, password);
    }

    /**
     * 测试版本路由 - 调用 V2.0.0 版本
     * 
     * <p><b>接口功能：</b></p>
     * 与 testVersionV1 类似，但目标是 V2.0.0 版本。
     * 用于验证新版功能是否正常工作。
     * 
     * <p><b>预期结果：</b></p>
     * 返回内容应包含 "[V2.0.0]" 前缀，
     * 以及 BCrypt、MFA、日志等新特性说明。
     * 
     * @param username 用户名
     * @param password 密码
     * @return String V2 版本的登录结果
     *         HTTP GET /governance/version/v2/admin/123456
     */
    @GetMapping("/version/v2/{username}/{password}")
    public String testVersionV2(@PathVariable String username, @PathVariable String password) {
        return "[版本路由] 调用 V2.0.0 服务:\n" + v2UserService.userLogin(username, password);
    }

    /**
     * 测试版本通配符 - 匹配任意版本
     * 
     * <p><b>接口功能：</b></p>
     * 使用 version="*" 配置引用，观察 Dubbo 如何选择服务版本。
     * 由于使用了通配符，可能会路由到 V1 或 V2，取决于负载均衡策略。
     * 
     * <p><b>实验方法：</b></p>
     * <ol>
     *   <li>多次访问该接口</li>
     *   <li>观察返回结果中的版本号是 [V1.0.0] 还是 [V2.0.0]</li>
     *   <li>统计各版本被选中的频率</li>
     * </ol>
     * 
     * <p><b>学习要点：</b></p>
     * 通配符虽然方便，但在生产环境中可能带来不确定性。
     * 更好的做法是通过配置中心动态控制版本流向。
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 任意版本的登录结果
     *         HTTP GET /governance/version/any/admin/123456
     */
    @GetMapping("/version/any/{username}/{password}")
    public String testAnyVersion(@PathVariable String username, @PathVariable String password) {
        return "[版本路由] version='*' 匹配任意版本:\n" + anyVersionUserService.userLogin(username, password);
    }

    /**
     * 测试标签路由（Tag Router）- 金丝雀发布
     * 
     * <p><b>什么是标签路由？</b></p>
     * 标签路由允许你在一次请求中携带自定义的"标签"（Tag），
     * Dubbo 根据这个标签将请求路由到打了相同标签的 Provider 子集。
     * 
     * <p><b>典型场景：金丝雀发布（Canary Release）</b></p>
     * <pre>
     * 总用户量: 10,000
     * 
     * 普通用户（99%）:
     * └── tag=null 或 tag=stable
     *     └── Provider-A (稳定版 V1.0.0)
     * 
     * 金丝雀用户（1%）:
     * └── tag=canary
     *     └── Provider-B (新版本 V2.0.0)
     * 
     * 流程：
     * 1. 先让1%的用户试用新版本
     * 2. 监控错误率、响应时间等指标
     * 3. 如果一切正常，逐步扩大 canary 用户比例
     * 4. 最终全部切换到新版本
     * </pre>
     * 
     * <p><b>如何配置标签路由？（Nacos 中）</b></p>
     * <pre>
     * 在 Nacos 的配置管理中创建如下规则：
     * 
     * Key: dubbo.consumer.tag-router-rule
     * Value:
     *   force: false
     *   enabled: true
     *   runtime: true
     *   tags:
     *     - name: canary
     *       match:
     *         - key: tag
     *           value: canary
     * </pre>
     * 
     * <p><b>⚠️ 当前状态：</b><br>
     * 如果未在 Nacos 中配置标签规则，或者没有标记为 "canary" 的 Provider，
     * 该接口可能会抛出 "No provider available" 异常。<br>
     * 这属于正常现象，说明标签路由正在生效（只是找不到匹配的目标）。</p>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 金丝雀版本的登录结果，或错误提示信息
     *         HTTP GET /governance/tag/canary/admin/123456
     */
    @GetMapping("/tag/canary/{username}/{password}")
    public String testTagRouter(@PathVariable String username, @PathVariable String password) {
        try {
            return "[标签路由] 请求带 canary 标签的服务:\n" 
                    + anyVersionUserService.userLogin(username, password);
        } catch (Exception e) {
            return "[标签路由] 未找到匹配的标签服务: " + e.getMessage()
                    + "\n提示: 请在 Nacos 配置 tag 规则或使用 Consumer 端参数: tag=canary";
        }
    }

    /**
     * 测试条件路由（Condition Router）- 环境隔离示例
     * 
     * <p><b>什么是条件路由？</b></p>
     * 条件路由允许你定义一组<strong>布尔表达式规则</strong>，
     * 根据 Consumer 或 Provider 的元数据（如应用名、IP、版本等）
     * 来决定请求的路由方向。
     * 
     * <p><b>规则语法：</b></p>
     * <pre>
     * [consumer条件] => [provider条件]
     * 
     * 示例：
     * host != 10.57.* => host = 10.57.*
     * 含义：非 10.57.x 网段的消费者只能访问 10.57.x 网段的生产者
     * 
     * application == order-service => application != test-service
     * 含义：订单服务不能访问测试环境的提供者
     * </pre>
     * 
     * <p><b>常见应用场景：</b></p>
     * <ol>
     *   <li><b>黑名单</b>: 封禁恶意 IP 或故障节点</li>
     *   <li><b>白名单</b>: 只允许内部服务互相调用</li>
     *   <li><b>机房隔离</b>: 同机房优先调用，减少跨机房延迟</li>
     *   <li><b>环境隔离</b>: dev/test/prod 环境互不干扰</li>
     *   <li><b>读写分离</b>: 写操作走主库，读操作走从库</li>
     * </ol>
     * 
     * <p><b>本接口的示例规则：</b></p>
     * <pre>
     * consumer应用 == nacos-consumer => provider应用 != nacos-provider-test
     * 
     * 解释：
     * 当消费者是 nacos-consumer 时，
     * 只能调用非测试环境（test）的提供者。
     * 这可以防止生产环境误调用测试服务。
     * </pre>
     * 
     * <p><b>如何配置条件路由？（Nacos 中）</b></p>
     * <pre>
     * Key: dubbo.consumer.condition-router-rule
     * Value:
     *   force: false
     *   enabled: true
     *   conditions:
     *     - "application == nacos-consumer => application != nacos-provider-test"
     * </pre>
     * 
     * @param username 用户名（可作为路由条件的输入参数）
     * @param password 密码
     * @return String 包含条件路由规则说明和实际调用结果的详细信息
     *         HTTP GET /governance/condition/admin/123456
     */
    @GetMapping("/condition/{username}/{password}")
    public String testConditionRouter(@PathVariable String username, @PathVariable String password) {
        return """
                [条件路由演示]
                
                当前请求参数:
                - username: %s
                
                条件路由规则示例 (在 Nacos 中配置):
                ====> consumer应用 == nacos-consumer => provider应用 != nacos-provider-test
                
                说明:
                - 当消费者是 nacos-consumer 时
                - 只调用非测试环境的提供者
                - 可用于环境隔离 (dev/test/prod)
                
                实际调用结果:
                """.formatted(username) + anyVersionUserService.userLogin(username, password);
    }

    /**
     * 测试分组路由（Group Routing）- 多租户隔离示例
     * 
     * <p><b>什么是分组（Group）？</b></p>
     * 分组是 Dubbo 中用于<strong>逻辑隔离</strong>服务的机制。
     * 同一个接口（如 UserService）可以有多个不同的分组实现，
     * 每个分组代表一种特定的业务含义或租户。
     * 
     * <p><b>Group vs Version 的区别：</b></p>
     * <table border="1">
     *   <tr>
     *     <th>维度</th>
     *     <th>Version（版本）</th>
     *     <th>Group（分组）</th>
     *   </tr>
     *   <tr>
     *     <td>语义</td>
     *     <td>同一功能的演进迭代</td>
     *     <td>不同功能或租户的实现</td>
     *   </tr>
     *   <tr>
     *     <td>关系</td>
     *     <td>V2 是 V1 的升级版</td>
     *     <td>A组和B组相互独立</td>
     *   </tr>
     *   <tr>
     *     <td>共存</td>
     *     <td>通常只保留最新版</td>
     *     <td>多个组长期并存</td>
     *   </tr>
     *   <tr>
     *     <td>示例</td>
     *     <td>V1.0 → V2.0</td>
     *     <td>普通版/VIP版/企业版</td>
     *   </tr>
     * </table>
     * 
     * <p><b>典型应用场景：</b></p>
     * <ol>
     *   <li><b>多租户 SaaS 平台</b>
     *       <ul>
     *         <li>Tenant-A → group="tenant-a"</li>
     *         <li>Tenant-B → group="tenant-b"</li>
     *         <li>数据隔离，配置独立</li>
     *       </ul>
     *   </li>
     *   <li><b>业务模块划分</b>
     *       <ul>
     *         <li>订单服务 → group="order"</li>
     *         <li>支付服务 → group="payment"</li>
     *         <li>物流服务 → group="logistics"</li>
     *       </ul>
     *   </li>
     *   <li><b>环境区分</b>
     *       <ul>
     *         <li>开发环境 → group="dev"</li>
     *         <li>测试环境 → group="test"</li>
     *         <li>生产环境 → group="prod"</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><b>本示例中的 Group：</b></p>
     * <pre>
     * group = "governance-version"
     * 
     * 这是一个专门用于演示服务治理功能的分组，
     * 包含了 V1 和 V2 两个版本的服务实现。
     * </pre>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 分组信息和调用结果
     *         HTTP GET /governance/group/demo/admin/123456
     */
    @GetMapping("/group/demo/{username}/{password}")
    public String testGroupRouting(@PathVariable String username, @PathVariable String password) {
        return """
                [分组路由演示]
                
                Group: governance-version
                
                分组的作用:
                1. 服务逻辑分组 (如: 按业务模块)
                2. 多租户隔离
                3. 不同配置的服务实例
                
                调用结果:
                """ + anyVersionUserService.userLogin(username, password);
    }
}
