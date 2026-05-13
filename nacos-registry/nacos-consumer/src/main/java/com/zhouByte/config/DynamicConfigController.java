package com.zhouByte.config;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动态配置测试控制器
 * 
 * <h2>功能说明</h2>
 * 该控制器全面演示了 Dubbo 的<strong>动态配置管理</strong>能力，
 * 包括当前配置查看、配置覆盖机制、自定义参数传递、方法级粒度配置等高级特性。
 * 
 * <h2>为什么需要动态配置？</h2>
 * <p>在传统的单体应用中，修改配置通常需要：</p>
 * <ol>
 *   <li>修改配置文件（application.yml）</li>
 *   <li>重新打包应用（mvn package）</li>
 *   <li>停止旧版本、部署新版本</li>
 *   <li>等待应用启动完成</li>
 *   <li>验证配置是否生效</li>
 * </ol>
 * <p>整个过程可能需要<strong>几分钟到几十分钟</strong>，对于生产环境来说是不可接受的。</p>
 * 
 * <p>有了动态配置后，流程变为：</p>
 * <ol>
 *   <li>打开 Nacos 控制台</li>
 *   <li>找到对应的服务配置</li>
 *   <li>修改参数值并发布</li>
 *   <li>配置<strong>秒级生效</strong>！✅</li>
 * </ol>
 * 
 * <h2>Dubbo 配置覆盖机制详解</h2>
 * <p>当同一参数在多个层级都有定义时，Dubbo 会按照优先级选择最高层的值：</p>
 * 
 * <h3>优先级排序（从高到低）</h3>
 * <table border="1">
 *   <tr>
 *     <th>优先级</th>
 *     <th>配置来源</th>
 *     <th>示例</th>
 *     <th>生效范围</th>
 *   </tr>
 *   <tr>
 *     <td>1（最高）</td>
 *     <td>JVM -D 参数</td>
 *     <td>-Ddubbo.consumer.timeout=5000</td>
 *     <td>全局</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>Consumer/Provider 方法级 @Method</td>
 *     <td>@Method(name="login", timeout=2000)</td>
 *     <td>单个方法</td>
 *   </tr>
 *   <tr>
 *     <td>3</td>
 *     <td>Consumer/Provider 接口级注解</td>
 *     <td>@DubboReference(timeout=3000)</td>
 *     <td>单个服务</td>
 *   </tr>
 *   <tr>
 *     <td>4</td>
 *     <td>全局配置（application.yaml）</td>
 *     <td>dubbo.consumer.timeout=4000</td>
 *     <td>所有服务</td>
 *   </tr>
 *   <tr>
 *     <td>5（最低）</td>
 *     <td>Dubbo 框架默认值</td>
 *     <td>timeout=1000ms</td>
 *     <td>兜底</td>
 *   </tr>
 * </table>
 * 
 * <h2>REST API 接口列表</h2>
 * <table border="1">
 *   <tr>
 *     <th>HTTP 方法</th>
 *     <th>路径</th>
 *     <th>功能描述</th>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/dynamic-config/current/{user}/{pwd}</td>
 *     <td>查看当前配置及调用结果</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/dynamic-config/override/demo/{user}/{pwd}</td>
 *     <td>演示配置覆盖机制</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/dynamic-config/parameters/{user}/{pwd}</td>
 *     <td>自定义参数传递演示</td>
 *   </tr>
 *   <tr>
 *     <td>GET</td>
 *     <td>/dynamic-config/method-level/{user}/{pwd}</td>
 *     <td>方法级粒度配置演示</td>
 *   </tr>
 * </table>
 * 
 * <h2>前置条件</h2>
 * <ul>
 *   <li>Nacos 注册中心 + 配置中心已启动</li>
 *   <li>nacos-provider 已启动（包含 ConfigurableUserService）</li>
 *   <li>nacos-consumer 已启动（即当前应用）</li>
 * </ul>
 * 
 * @author zhouByte
 * @version 1.0.0
 * @see UserService
 * @see ConfigurableUserService
 */
@RestController
@RequestMapping("/dynamic-config")
public class DynamicConfigController {

    /**
     * 动态配置服务引用 - Consumer 端配置
     * 
     * <p><b>配置说明：</b></p>
     * <ul>
     *   <li><b>group/version</b>: 定位到 dynamic-config 组的 1.0.0 版本服务</li>
     *   <li><b>check=false</b>: 启动时不检查 Provider 是否可用（允许延迟依赖）</li>
     *   <li><b>methods</b>: 方法级配置，针对 userLogin 方法的特殊设置</li>
     *   <li><b>parameters</b>: 自定义键值对参数，可透传给 Provider</li>
     * </ul>
     * 
     * <p><b>⚠️ 注意：Consumer 和 Provider 的配置可能会冲突！</b></p>
     * 当两端的 timeout/retries 等参数不一致时：
     * <ul>
     *   <li>通常以 <strong>Consumer 端</strong>为准（因为 Consumer 发起调用）</li>
     *   <li>但某些场景下 Provider 的配置也会影响行为</li>
     *   <li>建议保持两端配置一致，或明确了解差异的影响</li>
     * </ul>
     */
    @DubboReference(
            interfaceClass = UserService.class,
            group = "dynamic-config",
            version = "1.0.0",
            check = false,
            methods = {
                    @Method(
                            name = "userLogin",
                            timeout = 5000,
                            retries = 3,
                            loadbalance = "roundrobin"
                    )
            },
            parameters = {
                    "timeout", "5000",
                    "retries", "3"
            }
    )
    private UserService dynamicConfigUserService;

    /**
     * 测试当前动态配置状态
     * 
     * <p><b>接口功能：</b></p>
     * 展示 Consumer 端当前的配置参数，并实际调用远程服务获取 Provider 端的信息。
     * 通过对比两端信息，可以验证配置是否按预期工作。
     * 
     * <p><b>输出内容：</b></p>
     * <ol>
     *   <li>Consumer 端配置摘要（timeout、retries、loadbalance）</li>
     *   <li>Provider 端的实际返回结果（包含其自身配置信息）</li>
     * </ol>
     * 
     * <p><b>实验步骤：</b></p>
     * <ol>
     *   <li>首次访问：记录当前的配置和结果</li>
     *   <li>去 Nacos 修改某个参数（如 timeout 从5000改为2000）</li>
     *   <li>再次访问：观察是否有变化（取决于配置推送机制）</li>
     *   <li>如果没变化：可能需要触发配置刷新或重启 Consumer</li>
     * </ol>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 当前配置信息 + 调用结果
     *         HTTP GET /dynamic-config/current/admin/123456
     */
    @GetMapping("/current/{username}/{password}")
    public String testCurrentConfig(@PathVariable String username, @PathVariable String password) {
        return """
                [动态配置 - 当前配置]
                
                Consumer 端配置:
                - timeout: 5000ms
                - retries: 3次
                - loadbalance: roundrobin
                
                调用结果:
                """ + dynamicConfigUserService.userLogin(username, password);
    }

    /**
     * 演示配置覆盖机制
     * 
     * <p><b>接口功能：</b></p>
     * 详细解释 Dubbo 的多层配置覆盖机制，
     * 并提供在 Nacos 中进行动态配置的具体示例。
     * 
     * <p><b>核心概念：</b></p>
     * 同一个参数可能在多个地方被定义，Dubbo 会按照优先级选择最终生效的值。
     * 这类似于 CSS 中的样式层叠（Cascading），或者编程语言中的变量作用域。
     * 
     * <p><b>Nacos 配置示例：</b></p>
     * <pre>
     * 方式1：通过 Properties 配置
     * Key: dubbo.consumer.com.zhouByte.api.UserService.timeout
     * Value: 8000
     * 
     * 方式2：通过 Override URL（更灵活）
     * override://0.0.0.0/com.zhouByte.api.UserService?
     *   category=configurators&
     *   dynamic=false&
     *   timeout=10000
     * </pre>
     * 
     * <p><b>何时使用哪种方式？</b></p>
     * <ul>
     *   <li><b>Properties</b>: 适合简单的键值对配置，易于理解</li>
     *   <li><b>Override URL</b>: 适合复杂的规则配置，支持更多选项</li>
     * </ul>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 配置覆盖机制的详细说明 + 调用结果
     *         HTTP GET /dynamic-config/override/demo/admin/123456
     */
    @GetMapping("/override/demo/{username}/{password}")
    public String testOverrideConfig(@PathVariable String username, @PathVariable String password) {
        return """
                [动态配置覆盖机制]
                
                Dubbo 配置优先级 (从高到低):
                1. JVM -D 参数 (最高)
                2. Consumer/Provider 方法级 @Method 注解
                3. Consumer/Provider 级别 @DubboReference/@DubboService
                4. 全局配置 (application.yaml)
                5. 注册中心动态配置 (Nacos Config)
                
                演示场景:
                在 Nacos 控制台添加以下配置，无需重启即可生效:
                
                ====> 配置示例 <====
                Key: dubbo.consumer.com.zhouByte.api.UserService.timeout
                Value: 8000
                
                或使用 Nacos 的规则配置:
                ====> Override URL <====
                override://0.0.0.0/com.zhouByte.api.UserService?category=configurators&dynamic=false&timeout=10000
                
                当前调用:
                """ + dynamicConfigUserService.userLogin(username, password);
    }

    /**
     * 自定义参数传递演示
     * 
     * <p><b>接口功能：</b></p>
     * 除了 Dubbo 内置的标准参数外，还支持传递自定义的业务参数。
     * 这些参数可以在 Consumer 和 Provider 之间透传，用于业务逻辑判断。
     * 
     * <p><b>使用方式：</b></p>
     * <pre>
     * // Consumer 端设置
     * @DubboReference(
     *     parameters = {
     *         "tenantId", "TENANT_001",
     *         "requestSource", "MOBILE_APP",
     *         "featureFlag", "new-login-ui"
     *     }
     * )
     * private UserService userService;
     * 
     * // Provider 端读取
     * RpcContext.getServerContext().getUrl().getParameter("tenantId");
     * // 返回: "TENANT_001"
     * </pre>
     * 
     * <p><b>典型应用场景：</b></p>
     * <ol>
     *   <li><b>租户ID透传</b>: SaaS 多租户系统中标识当前租户</li>
     *   <li><b>请求来源标记</b>: 区分 WEB/H5/App/第三方调用</li>
     *   <li><b>功能开关</b>: A/B 测试中的实验组标记</li>
     *   <li><b>链路追踪ID</b>: 分布式追踪系统中的 TraceID 传递</li>
     *   <li><b>灰度标签</b>: 金丝雀发布中的流量染色</li>
     * </ol>
     * 
     * <p><b>⚠️ 注意事项：</b></p>
     * <ul>
     *   <li>自定义参数只适用于简单场景，复杂建议使用 Attachment（隐式传参）</li>
     *   <li>参数值会被注册中心持久化，敏感信息请勿使用此方式</li>
     *   <li>参数过多会影响 URL 长度和性能</li>
     * </ul>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 自定义参数的使用说明 + 调用结果
     *         HTTP GET /dynamic-config/parameters/admin/123456
     */
    @GetMapping("/parameters/{username}/{password}")
    public String testCustomParameters(@PathVariable String username, @PathVariable String password) {
        return """
                [自定义参数传递]
                
                除了标准参数外，还可以传递自定义参数:
                - parameters = {"key1": "value1"}
                
                使用场景:
                1. 业务标识透传
                2. 租户 ID 传递
                3. 功能开关控制
                4. A/B 测试标记
                
                Provider 端可通过以下方式获取:
                RpcContext.getContext().getUrl().getParameter("key1")
                
                调用结果:
                """ + dynamicConfigUserService.userLogin(username, password);
    }

    /**
     * 方法级粒度配置演示
     * 
     * <p><b>接口功能：</b></p>
     * 展示 Dubbo 支持对<strong>同一个服务的不同方法</strong>应用不同的配置。
     * 这是比接口级配置更细粒度的控制手段。
     * 
     * <p><b>为什么需要方法级配置？</b></p>
     * <pre>
     * 场景：订单服务有两个核心方法
     * 
     * 1. createOrder() - 创建订单（写操作）
     *    - 要求：快速失败（Failfast）、不重试、短超时
     *    - 原因：避免重复创建订单
     *    
     * 2. queryOrder() - 查询订单（读操作）
     *    - 要求：高可用（Failover）、可重试、长超时
     *    - 原因：查询操作幂等，可以容忍一定延迟
     * 
     * 如果只有接口级配置，无法同时满足这两种需求！
     * 解决方案：使用 @Method 注解分别配置
     * </pre>
     * 
     * <p><b>配置示例：</b></p>
     * <pre>
     * @DubboReference(
     *     interfaceClass = OrderService.class,
     *     methods = {
     *         @Method(
     *             name = "createOrder",
     *             timeout = 2000,
     *             retries = 0,
     *             cluster = "failfast"
     *         ),
     *         @Method(
     *             name = "queryOrder",
     *             timeout = 10000,
     *             retries = 3,
     *             cluster = "failover"
     *         )
     *     }
     * )
     * </pre>
     * 
     * <p><b>优势总结：</b></p>
     * <ul>
     *   <li><b>精细化控制</b>: 不同方法可以有完全不同的行为特征</li>
     *   <li><b>性能优化</b>: 为关键路径和非关键路径设置不同的 SLA</li>
     *   <li><b>差异化容错</b>: 写操作严格，读操作宽松</li>
     *   <li><b>灵活性</b>: 无需拆分服务即可实现差异化配置</li>
     * </ul>
     * 
     * @param username 用户名
     * @param password 密码
     * @return String 方法级配置的优势说明 + 调用结果
     *         HTTP GET /dynamic-config/method-level/admin/123456
     */
    @GetMapping("/method-level/{username}/{password}")
    public String testMethodLevelConfig(@PathVariable String username, @PathVariable String password) {
        return """
                [方法级粒度配置]
                
                同一服务的不同方法可以有不同的配置:
                
                @Method(name="login", timeout=2000)   // 登录接口快响应
                @Method(name="query", timeout=10000)   // 查询接口可慢
                
                优势:
                - 细粒度控制
                - 针对性优化
                - 不同业务场景差异化处理
                
                调用结果:
                """ + dynamicConfigUserService.userLogin(username, password);
    }
}
