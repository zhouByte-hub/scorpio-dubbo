package com.zhouByte.advanced;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.HashMap;
import java.util.Map;

/**
 * 泛化服务实现类 - 实现 Dubbo 泛化调用（Generic Invocation）的服务端
 *
 * <h2>功能概述</h2>
 * 该类实现了 Dubbo 的 GenericService 接口，允许 Consumer 在没有接口依赖的情况下
 * 动态调用远程服务。这是 Dubbo 提供的高级特性之一，特别适用于网关、测试平台等场景。
 *
 * <h2>什么是泛化调用？</h2>
 * <p>传统 Dubbo 调用流程：
 * <pre>
 * 1. Consumer 必须依赖 Provider 的接口 JAR 包
 * 2. 通过 @DubboReference 引用具体接口类型
 * 3. 编译期检查方法签名是否匹配
 * 4. 运行时直接调用接口方法
 * </pre>
 *
 * <p>泛化调用流程：
 * <pre>
 * 1. Consumer 无需任何接口依赖
 * 2. 通过 GenericService 接口统一调用
 * 3. 动态指定：接口名、方法名、参数类型、参数值
 * 4. 返回值为 Object 类型（通常是 Map/List）
 * </pre>
 *
 * <h2>核心原理</h2>
 * <ol>
 *   <li><b>反射机制</b> - Provider 端通过 $invoke 方法接收方法名和参数，利用反射调用真实实现</li>
 *   <li><b>动态代理</b> - Consumer 端创建通用代理，将 $invoke 转换为标准 RPC 请求</li>
 *   <li><b>类型擦除</b> - 编译期不检查类型，所有类型信息在运行时传递</li>
 *   <li><b>协议兼容</b> - 底层仍使用 Dubbo 协议，只是上层封装不同</li>
 * </ol>
 *
 * <h2>$invoke 方法详解</h2>
 * <pre>
 * public Object $invoke(
 *     String method,          // 方法名，例如 "login"、"getUserInfo"
 *     String[] parameterTypes, // 参数类型的全限定名数组
 *                           // 例如 ["java.lang.String", "java.lang.Integer"]
 *     Object[] args           // 参数值数组，必须与 parameterTypes 一一对应
 * ) throws GenericException;
 *
 * 返回值：Object（通常是 Map&lt;String, Object&gt; 或 List&lt;Object&gt;）
 * </pre>
 *
 * <h2>典型应用场景</h2>
 * <table border="1">
 *   <tr><th>场景</th><th>说明</th><th>优势</th></tr>
 *   <tr>
 *     <td>API 网关</td>
 *     <td>统一入口转发到不同微服务</td>
 *     <td>无需引入各服务的接口包</td>
 *   </tr>
 *   <tr>
 *     <td>测试平台</td>
 *     <td>自动化测试工具动态构造请求</td>
 *     <td>支持任意方法的测试用例生成</td>
 *   </tr>
 *   <tr>
 *     <td>服务 Mock</td>
 *     <td>开发环境模拟依赖服务</td>
 *     <td>快速搭建测试桩</td>
 *   </tr>
 *   <tr>
 *     <td>多版本适配</td>
 *     <td>同时对接多个版本的同一服务</td>
 *     <td>灵活切换，无需重新编译</td>
 *   </tr>
 *   <tr>
 *     <td>第三方集成</td>
 *     <td>允许外部系统调用内部服务</td>
 *     <td>降低耦合度，便于权限控制</td>
 *   </tr>
 * </table>
 *
 * <h2>配置要点</h2>
 * <pre>
 * @DubboService 注解的关键属性：
 *
 * 1. generic = "true"  ← 必须！标记为泛化服务
 *    告诉 Dubbo 框架这是一个泛化实现，而不是普通服务
 *
 * 2. interfaceClass = GenericService.class  ← 固定值
 *    所有泛化服务都实现这个统一接口
 *
 * 3. parameters = {"interface", "..."}
 *    声明实际的业务接口全限定名
 *    用于注册中心发现和服务路由
 *
 * 示例配置：
 * @DubboService(
 *     interfaceClass = GenericService.class,
 *     group = "generic",
 *     version = "1.0.0",
 *     generic = "true",
 *     parameters = {
 *         "interface", "com.zhouByte.api.UserService",
 *         "generic", "true"
 *     }
 * )
 * </pre>
 *
 * <h2>Consumer 端调用示例</h2>
 * <pre>
 * // 1. 创建 ReferenceConfig
 * ReferenceConfig&lt;GenericService&gt; reference = new ReferenceConfig&lt;&gt;();
 * reference.setInterface("com.zhouByte.api.UserService");  // 目标接口
 * reference.setVersion("1.0.0");
 * reference.setGroup("generic");
 * reference.setGeneric("true");  // 开启泛化模式
 *
 * // 2. 从缓存获取代理对象
 * ReferenceConfigCache cache = ReferenceConfigCache.getCache();
 * GenericService genericService = cache.get(reference);
 *
 * // 3. 动态调用
 * Object result = genericService.$invoke(
 *     "userLogin",                                    // 方法名
 *     new String[]{"java.lang.String", "java.lang.String"},  // 参数类型
 *     new Object[]{"admin", "123456"}                 // 参数值
 * );
 *
 * // 4. 处理返回值（Map 格式）
 * Map&lt;String, Object&gt; map = (Map&lt;String, Object&gt;) result;
 * String message = (String) map.get("message");
 * </pre>
 *
 * <h2>优缺点分析</h2>
 * <h3>优点</h3>
 * <ul>
 *   <li>✅ 解耦彻底 - Consumer 完全不依赖 Provider 的接口定义</li>
 *   <li>✅ 灵活度高 - 支持运行时动态决定调用哪个方法</li>
 *   <li>✅ 易于扩展 - 新增服务无需修改网关代码</li>
 *   <li>✅ 适合平台化产品 - 如 API 管理平台、服务测试平台</li>
 * </ul>
 *
 * <h3>缺点</h3>
 * <ul>
 *   <li>❌ 无编译期检查 - 参数类型错误只能在运行时发现</li>
 *   <li>❌ 性能损耗 - 反射调用比直接调用慢 10-20%</li>
 *   <li>❌ 代码可读性差 - 大量的类型转换和 Map 操作</li>
 *   <li>❌ IDE 支持弱 - 无法跳转到方法定义、无自动补全</li>
 *   <li>❌ 返回值处理繁琐 - 需要手动从 Map 中提取字段</li>
 * </ul>
 *
 * <h2>性能优化建议</h2>
 * <ul>
 *   <li>使用 ReferenceConfigCache 缓存代理对象（避免重复创建）</li>
 *   <li>对于高频调用的方法，考虑生成专用代理类</li>
 *   <li>批量操作时优先使用普通接口调用</li>
 *   <li>监控泛化调用的 QPS 和 P99 延迟</li>
 * </ul>
 *
 * <h2>安全注意事项</h2>
 * <ul>
 *   <li>泛化服务可能成为攻击面（可调用任意公开方法）</li>
 *   <li>建议添加白名单机制，限制允许调用的方法</li>
 *   <li>对参数进行严格校验，防止注入攻击</li>
 *   <li>记录所有泛化调用的审计日志</li>
 * </ul>
 *
 * @author zhouByte
 * @version 1.0.0
 * @see org.apache.dubbo.rpc.service.GenericService
 * @see org.apache.dubbo.rpc.service.GenericException
 */
@DubboService(
        interfaceClass = GenericService.class,
        group = "generic",
        version = "1.0.0",
        generic = "true",
        parameters = {
                "interface", "com.zhouByte.api.UserService",
                "generic", "true"
        }
)
public class GenericServiceImpl implements GenericService {

    /**
     * 泛化调用的核心方法 - 处理所有来自 Consumer 的动态请求
     *
     * <h3>方法签名说明</h3>
     * <pre>
     * $invoke 是 GenericService 接口的唯一方法
     * 所有泛化调用都会路由到这里
     *
     * @param method 方法名称
     *        - 必须与目标接口的方法名完全匹配
     *        - 区分大小写
     *        - 例如："userLogin"、"getUserById"
     *
     * @param parameterTypes 参数类型数组
     *        - 每个元素是参数类型的全限定类名
     *        - 基本类型使用包装类：int → java.lang.Integer
     *        - 数组类型加前缀：String[] → "[Ljava.lang.String;"
     *        - 示例：["java.lang.String", "java.lang.Integer"]
     *
     * @param args 参数值数组
     *        - 与 parameterTypes 一一对应
     *        - 数组长度必须相等
     *        - 类型必须匹配（否则反序列化失败）
     *
     * @return Object 返回值
     *        - 通常是 Map&lt;String, Object&gt; 结构化数据
     *        - 也可能是 List&lt;Object&gt;、基本类型等
     *        - Consumer 需要知道具体的结构才能解析
     *
     * @throws GenericException 当发生以下情况时抛出：
     *         - 方法不存在
     *         - 参数类型不匹配
     *         - 业务逻辑执行异常
     * </pre>
     *
     * <h3>实现策略</h3>
     * 本类采用 switch-case 分发模式：
     * <pre>
     * switch (method) {
     *     case "method1":
     *         return handleMethod1(args);
     *     case "method2":
     *         return handleMethod2(args);
     *     default:
     *         throw new GenericException(...);
     * }
     * </pre>
     *
     * <h3>更高级的实现方式</h3>
     * 对于方法很多的接口，可以考虑：
     * <ul>
     *   <li>反射调用真实的 Service Bean（完全动态）</li>
     *   <li>使用方法名 → MethodHandler 的 Map 映射</li>
     *   <li>结合 Spring 的 ApplicationContext 获取目标 Bean</li>
     * </ul>
     *
     * @param method 要调用的方法名
     * @param parameterTypes 参数类型的全限定名数组
     * @param args 参数值数组
     * @return Object 类型的调用结果
     * @throws GenericException 当方法不支持或参数错误时
     */
    @Override
    public Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
        
        switch (method) {
            case "userLogin":
                return handleUserLogin(args);
            
            default:
                throw new GenericException(new UnsupportedOperationException("不支持的方法: " + method));
        }
    }

    /**
     * 处理用户登录请求 - 演示泛化调用的参数提取和结果构建
     *
     * <h3>参数处理</h3>
     * <pre>
     * args 数组的结构：
     * args[0] → username (String)
     * args[1] → password (String)
     *
     * 注意事项：
     * - args 可能为 null（Consumer 未传参数）
     * - args.length 可能 &lt; 2（参数数量不足）
     * - 元素类型可能与预期不符（Consumer 传错类型）
     * - 需要进行防御性编程
     * </pre>
     *
     * <h3>返回值设计</h3>
     * <p>采用 Map&lt;String, Object&gt; 结构，包含以下字段：
     * <table border="1">
     *   <tr><th>字段</th><th>类型</th><th>含义</th></tr>
     *   <tr><td>code</td><td>Integer</td><td>状态码（200=成功，其他=失败）</td></tr>
     *   <tr><td>message</td><td>String</td><td>人类可读的消息</td></tr>
     *   <tr><td>data</td><td>String</td><td>业务数据载荷</td></tr>
     *   <tr><td>username</td><td>String</td><td>回显的用户名（便于追踪）</td></tr>
     *   <tr><td>loginTime</td><td>Long</td><td>服务器时间戳（毫秒）</td></tr>
     *   <tr><td>callType</td><td>String</td><td>标识这是泛化调用（便于区分）</td></tr>
     * </table>
     *
     * <h3>为什么使用 Map 而不是自定义 DTO？</h3>
     * <ul>
     *   <li>泛化调用的 Consumer 没有依赖，无法识别自定义类</li>
     *   <li>Map 是通用的数据结构，所有语言都能解析</li>
     *   <li>JSON 序列化/反序列化天然支持 Map</li>
     *   <li>便于扩展字段而不影响现有 Consumer</li>
     * </ul>
     *
     * <h3>错误处理</h3>
     * <pre>
     * 当参数不合法时，有两种处理方式：
     *
     * 方式1：抛出异常（当前采用）
     *   throw new IllegalArgumentException("参数错误");
     *   → Dubbo 包装为 GenericException
     *   → Consumer 端收到 RpcException
     *
     * 方式2：返回错误码（推荐用于生产环境）
     *   Map&lt;String, Object&gt; errorResult = new HashMap&lt;&gt;();
     *   errorResult.put("code", 400);
     *   errorResult.put("message", "参数错误：需要 username 和 password");
     *   return errorResult;
     *   → Consumer 检查 code 字段判断成败
     * </pre>
     *
     * @param args 从 $invoke 方法传入的参数数组
     * @return Map&lt;String, Object&gt; 包含登录结果的结构化数据
     * @throws IllegalArgumentException 当参数缺失或类型错误时
     */
    private Object handleUserLogin(Object[] args) {
        if (args != null && args.length >= 2) {
            String username = (String) args[0];
            String password = (String) args[1];

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "泛化调用成功");
            result.put("data", "[GENERIC] " + username + " 通过泛化方式登录成功");
            result.put("username", username);
            result.put("loginTime", System.currentTimeMillis());
            result.put("callType", "Generic Invocation");

            return result;
        }
        
        throw new IllegalArgumentException("参数错误: 需要 username 和 password");
    }
}
