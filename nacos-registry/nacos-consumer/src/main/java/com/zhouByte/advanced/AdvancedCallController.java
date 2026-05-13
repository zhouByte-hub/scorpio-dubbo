package com.zhouByte.advanced;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 高级调用测试控制器 - 提供 REST API 测试 Dubbo 异步调用和泛化调用功能
 *
 * <h2>控制器职责</h2>
 * 该控制器作为 HTTP 入口，将 REST 请求转换为 Dubbo RPC 调用，用于演示和验证：
 * <ul>
 *   <li><b>异步调用</b> - 基于 CompletableFuture 的非阻塞 RPC</li>
 *   <li><b>链式异步</b> - 多个异步操作的组合与编排</li>
 *   <li><b>同步 vs 异步对比</b> - 直观展示性能差异</li>
 *   <li><b>泛化调用</b> - 无需接口依赖的动态 RPC 调用</li>
 * </ul>
 *
 * <h2>API 总览</h2>
 * <table border="1">
 *   <tr><th>路径</th><th>功能</th><th>HTTP 方法</th></tr>
 *   <tr><td>/advanced/async/{username}/{password}</td><td>基础异步调用测试</td><td>GET</td></tr>
 *   <tr><td>/advanced/async-chain/{username}/{password}</td><td>链式异步调用演示</td><td>GET</td></tr>
 *   <tr><td>/advanced/sync-vs-async/{username}/{password}</td><td>同步/异步性能对比</td><td>GET</td></tr>
 *   <tr><td>/advanced/generic/{username}/{password}</td><td>泛化调用演示</td><td>GET</td></tr>
 * </table>
 *
 * <h2>技术架构</h2>
 * <pre>
 * ┌─────────────┐     HTTP      ┌──────────────────────┐
 * │   Browser   │ ───────────→ │ AdvancedCallController │
 * └─────────────┘              └──────────┬───────────┘
 *                                         │
 *                    ┌────────────────────┼────────────────────┐
 *                    ↓                    ↓                    ↓
 *          ┌────────────────┐  ┌─────────────────┐  ┌─────────────────┐
 *          │ AsyncUserService│  │ AsyncUserService│  │ GenericService  │
 *          │ (asyncLogin)   │  │ (syncLogin)     │  │ ($invoke)       │
 *          └───────┬────────┘  └────────┬────────┘  └────────┬────────┘
 *                  ↓                    ↓                     ↓
 *          ┌────────────────┐  ┌─────────────────┐  ┌─────────────────┐
 *          │ AsyncUserSvcImpl│  │ AsyncUserSvcImpl│  │ GenericSvcImpl  │
 *          │ (线程池异步)    │  │ (同步阻塞)      │  │ (反射分发)      │
 *          └────────────────┘  └─────────────────┘  └─────────────────┘
 * </pre>
 *
 * <h2>使用指南</h2>
 * <h3>启动顺序</h3>
 * <ol>
 *   <li>启动 Nacos Server（默认 localhost:8848）</li>
 *   <li>启动 nacos-provider（注册 AsyncUserService、GenericService）</li>
 *   <li>启动 nacos-consumer（部署本 Controller）</li>
 *   <li>访问 http://localhost:{port}/advanced/async/admin/123456</li>
 * </ol>
 *
 * <h3>测试建议</h3>
 * <ul>
 *   <li>先用 /sync-vs-async/ 对比基础性能</li>
 *   <li>再用 /async-chain/ 观察链式编排效果</li>
 *   <li>最后用 /generic/ 理解泛化调用机制</li>
 *   <li>可结合 JMeter 进行压力测试，观察 QPS 差异</li>
 * </ul>
 *
 * <h2>返回值格式</h2>
 * <p>所有接口返回纯文本（text/plain），包含：
 * <ul>
 *   <li>功能说明和原理介绍</li>
 *   <li>时间统计和性能数据</li>
 *   <li>实际的 Dubbo 调用结果</li>
 *   <li>最佳实践建议</li>
 * </ul>
 *
 * @author zhouByte
 * @version 1.0.0
 * @see AsyncUserService
 * @see org.apache.dubbo.rpc.service.GenericService
 */
@RestController
@RequestMapping("/advanced")
public class AdvancedCallController {

    /**
     * 异步服务引用 - 用于发起异步 RPC 调用
     *
     * <p>Dubbo 会为该接口创建动态代理，调用 asyncLogin() 时：
     * <ol>
     *   <li>向 Provider 发送异步请求</li>
     *   <li>立即返回 CompletableFuture 对象（不阻塞）</li>
     *   <li>Provider 在后台线程执行业务逻辑</li>
     *   <li>完成后通过回调填充 Future 结果</li>
     * </ol>
     *
     * @see AsyncUserService#asyncLogin(String, String)
     */
    @DubboReference(
            interfaceClass = AsyncUserService.class,
            group = "advanced",
            version = "1.0.0"
    )
    private AsyncUserService asyncUserService;

    /**
     * 同步服务引用 - 用于性能对比测试
     *
     * <p>虽然引用的是同一个 AsyncUserService 接口，但我们只调用其 syncLogin()
     * 方法来模拟传统同步调用的行为，便于与异步模式做公平对比。
     *
     * <p>注意：变量名特意使用 syncUserServiceForComparison 以明确用途
     */
    @DubboReference(
            interfaceClass = AsyncUserService.class,
            group = "advanced",
            version = "1.0.0"
    )
    private AsyncUserService syncUserServiceForComparison;

    /**
     * 基础异步调用测试 - 展示 CompletableFuture 的基本用法
     *
     * <h3>测试目的</h3>
     * 验证异步调用的完整生命周期：
     * <ol>
     *   <li>提交请求并获得 Future（非阻塞）</li>
     *   <li>等待 Future 完成（可设置超时）</li>
     *   <li>获取最终结果</li>
     *   <li>统计各阶段耗时</li>
     * </ol>
     *
     * <h3>时间统计说明</h3>
     * <pre>
     * Timeline:
     * t=0ms    → 调用 asyncLogin()，立即返回 Future
     * t=1ms    → Future 对象已拿到（submitTime ≈ 1ms）
     * t=1ms~   → Consumer 线程可做其他事情（非阻塞！）
     * t=150ms  → Provider 处理完成，Future 被 complete
     * t=151ms  → 调用 future.get() 获取结果（如果有缓存则更快）
     * t=152ms  → 总耗时统计完毕
     *
     * 关键洞察：
     * - submitTime 应该 &lt; 5ms（只是网络往返+序列化）
     * - totalTime 主要由 Provider 的业务逻辑决定
     * - Consumer 线程在 submit~get 之间是自由的
     * </pre>
     *
     * <h3>Curl 测试命令</h3>
     * <pre>
     * curl http://localhost:8080/advanced/async/admin/123456
     * </pre>
     *
     * @param username 用户名（URL 路径参数）
     * @param password 密码（URL 路径参数）
     * @return String 包含详细的时间统计和调用结果
     * @throws ExecutionException 当 Provider 端抛出未处理的异常
     * @throws InterruptedException 当等待结果时线程被中断
     */
    @GetMapping("/async/{username}/{password}")
    public String testAsyncCall(@PathVariable String username, @PathVariable String password) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        CompletableFuture<String> future = asyncUserService.asyncLogin(username, password);

        long submitTime = System.currentTimeMillis() - startTime;

        String result = future.get();

        long totalTime = System.currentTimeMillis() - startTime;

        return """
                [异步调用演示]
                
                ⏱️ 时间统计:
                - 提交请求耗时: %dms (非阻塞，立即返回 Future 对象)
                - 等待结果耗时: %dms (阻塞等待实际结果)
                - 总耗时: %dms
                
                📝 调用特点:
                ✅ 不阻塞 Consumer 线程
                ✅ 返回 CompletableFuture 对象
                ✅ 可链式操作: thenApply / thenAccept / thenCompose
                ✅ 支持超时控制: get(timeout, unit)
                
                调用结果:
                """.formatted(submitTime, totalTime - submitTime, totalTime) 
                + result;
    }

    /**
     * 链式异步调用演示 - 展示 CompletableFuture 的组合能力
     *
     * <h3>场景描述</h3>
     * 模拟一个典型的业务流程：
     * <pre>
     * Step 1: 用户登录认证（RPC 调用，耗时 ~200ms）
     * Step 2: 查询用户权限（本地计算，耗时 ~50ms）
     * Step 3: 输出最终结果
     * </pre>
     *
     * <h3>为什么使用链式调用？</h3>
     * <pre>
     * 传统同步方式总耗时 = 200ms + 50ms = 250ms
     *
     * 链式异步方式：
     * - Step 1 和 Step 2 可以部分重叠（如果它们独立）
     * - 但本例中 Step 2 依赖 Step 1 的结果，所以是串行的
     * - 优势体现在：Step 1 完成后立即触发 Step 2，无需手动协调
     * </pre>
     *
     * <h3>CompletableFuture 方法说明</h3>
     * <ul>
     *   <li><b>thenApply</b> - 同步转换：接收上一个结果，返回新值（在同一线程执行）</li>
     *   <li><b>thenCompose</b> - 扁平映射：接收上一个结果，返回新的 Future（用于嵌套 Future）</li>
     *   <li><b>thenAccept</b> - 终端操作：消费结果，无返回值（通常用于副作用，如打印日志）</li>
     * </ul>
     *
     * <h3>执行流程图</h3>
     * <pre>
     * asyncLogin(user, pwd)
     *     │
     *     ▼ (完成后)
     * thenApply(result → authResult)
     *     │  转换: "ASYNC login success" → "用户 admin 已认证"
     *     ▼ (同步执行，很快)
     * thenCompose(authResult → ...)
     *     │  调用新的 CompletableFuture.supplyAsync(...)
     *     │  查询权限: "用户 admin 已认证 | 权限: [READ, WRITE]"
     *     ▼ (50ms后)
     * thenAccept(finalData → ...)
     *     │  打印最终结果
     *     ▼
     * finalResult.get()  ← 阻塞直到整个链路完成
     * </pre>
     *
     * @param username 用户名
     * @param password 密码
     * @return String 包含每个步骤的执行日志和时间统计
     * @throws Exception 当链路中任一步骤出错时
     */
    @GetMapping("/async-chain/{username}/{password}")
    public String testAsyncChainCall(@PathVariable String username, @PathVariable String password) throws Exception {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("[异步链式调用演示]\n\n");

        long startTime = System.currentTimeMillis();

        CompletableFuture<String> step1 = asyncUserService.asyncLogin(username, password)
                .thenApply(result -> {
                    logBuilder.append("Step 1: 登录完成 → ").append(result.substring(0, Math.min(result.length(), 50))).append("\n");
                    return "用户 " + username + " 已认证";
                });

        CompletableFuture<String> step2 = step1.thenCompose(authResult -> {
            logBuilder.append("Step 2: 开始查询权限...\n");
            return CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return authResult + " | 权限: [READ, WRITE]";
            });
        });

        CompletableFuture<String> finalResult = step2.thenAccept(finalData -> {
            logBuilder.append("Step 3: 最终结果 → ").append(finalData).append("\n");
        });

        finalResult.get();

        long totalTime = System.currentTimeMillis() - startTime;

        logBuilder.append(String.format("\n⏱️ 链式总耗时: %dms\n", totalTime));
        logBuilder.append("\n💡 优势说明:\n");
        logBuilder.append("- 多个异步操作可并行执行\n");
        logBuilder.append("- 减少总体等待时间\n");
        logBuilder.append("- 提升系统吞吐量\n");

        return logBuilder.toString();
    }

    /**
     * 同步 vs 异步性能对比 - 直观展示两种调用模式的差异
     *
     * <h3>对比维度</h3>
     * <table border="1">
     *   <tr><th>指标</th><th>同步调用</th><th>异步调用</th></tr>
     *   <tr><td>总耗时</td><td>≈ 业务耗时 + 网络耗时</td><td>≈ 业务耗时 + 网络耗时（相近）</td></tr>
     *   <tr><td>线程占用</td><td>整个调用期间持续占用</td><td>仅在 get() 时短暂占用</td></tr>
     *   <tr><td>并发能力</td><td>受线程池大小限制</td><td>理论上无限</td></tr>
     *   <tr><td>代码复杂度</td><td>简单线性</td><td>需要理解 Future API</td></tr>
     * </table>
     *
     * <h3>为什么单次调用性能相近？</h3>
     * <pre>
     * 单次调用场景：
     * Sync:  [阻塞 200ms] → 返回结果    总耗时 ≈ 200ms
     * Async: [提交 1ms] → [等待 199ms] → 返回结果  总耗时 ≈ 200ms
     *
     * 异步的优势不在单次调用，而在并发场景：
     *
     * 场景：需要同时调用 3 个独立服务（每个 200ms）
     * Sync:  200ms + 200ms + 200ms = 600ms（串行）
     * Async: max(200ms, 200ms, 200ms) = 200ms（并行）← 提升 3 倍！
     * </pre>
     *
     * <h3>何时选择异步？</h3>
     * <ul>
     *   <li>✅ 需要聚合多个独立服务的返回值</li>
     *   <li>✅ 系统 QPS 要求高（&gt; 1000）</li>
     *   <li>✅ 希望减少线程池资源占用</li>
     *   <li>✅ 后续逻辑不强制依赖当前结果</li>
     *   <li>❌ 只是简单的 CRUD 操作（过度设计）</li>
     *   <li>❌ 团队成员不熟悉 CompletableFuture</li>
     * </ul>
     *
     * @param username 用户名
     * @param password 密码
     * @return String 包含两种方式的耗时对比和分析结论
     * @throws Exception 当任一调用失败时
     */
    @GetMapping("/sync-vs-async/{username}/{password}")
    public String compareSyncAndAsync(@PathVariable String username, @PathVariable String password) throws Exception {
        long syncStart = System.currentTimeMillis();
        String syncResult = syncUserServiceForComparison.syncLogin(username, password);
        long syncCost = System.currentTimeMillis() - syncStart;

        long asyncStart = System.currentTimeMillis();
        CompletableFuture<String> asyncFuture = asyncUserService.asyncLogin(username, password);
        String asyncResult = asyncFuture.get();
        long asyncCost = System.currentTimeMillis() - asyncStart;

        return """
                [同步 vs 异步对比]
                
                🔵 同步调用:
                耗时: %dms
                结果: %s
                
                🟢 异步调用:
                耗时: %dms
                结果: %s
                
                📊 分析:
                - 单次调用性能相近
                - 异步优势体现在并发场景
                - 异步不阻塞线程，可同时处理其他任务
                - 高并发场景下，异步吞吐量更高
                """.formatted(syncCost, syncResult.replace("\n", " | "), 
                             asyncCost, asyncResult.replace("\n", " | "));
    }

    /**
     * 泛化调用测试 - 演示无需接口依赖的动态 RPC 调用
     *
     * <h3>核心概念</h3>
     * <p>泛化调用（Generic Invocation）允许 Consumer 在运行时动态指定：
     * <ul>
     *   <li>要调用的接口全限定名</li>
     *   <li>目标方法名</li>
     *   <li>参数类型列表</li>
     *   <li>参数值</li>
     * </ul>
     *
     * <h3>实现步骤详解</h3>
     * <pre>
     * 步骤1: 创建 ReferenceConfig（配置元数据）
     * ┌─────────────────────────────────────────────┐
     * │ ReferenceConfig&lt;GenericService&gt; ref = ...  │
     * │ ref.setInterface("com...UserService");      │  ← 目标接口
     * │ ref.setVersion("1.0.0");                    │  ← 版本号
     * │ ref.setGroup("generic");                    │  ← 分组
     * │ ref.setGeneric("true");                     │  ← 开启泛化
     * └─────────────────────────────────────────────┘
     *
     * 步骤2: 从缓存获取代理对象（性能优化）
     * ┌─────────────────────────────────────────────┐
     * │ ReferenceConfigCache cache =                 │
     *     ReferenceConfigCache.getCache();           │
     * │ GenericService service = cache.get(ref);    │  ← 代理对象
     * └─────────────────────────────────────────────┘
     * 注意：必须使用缓存！每次创建 ReferenceConfig 很昂贵
     *
     * 步骤3: 发起 $invoke 调用
     * ┌─────────────────────────────────────────────┐
     * │ Object result = service.$invoke(            │
     * │     "userLogin",                            │  ← 方法名
     * │     new String[]{"java.lang.String", ...},  │  ← 参数类型
     * │     new Object[]{"admin", "123456"}         │  ← 参数值
     * │ );                                          │
     * └─────────────────────────────────────────────┘
     *
     * 步骤4: 解析返回值（Map 结构）
     * ┌─────────────────────────────────────────────┐
     * │ Map&lt;String, Object&gt; map = (Map) result;    │
     * │ String msg = (String) map.get("message");   │
     * │ Integer code = (Integer) map.get("code");   │
     * └─────────────────────────────────────────────┘
     *
     * 步骤5: 销毁引用（资源清理）
     * cache.destroy(referenceConfig);  ← 必须调用！
     * </pre>
     *
     * <h3>ReferenceConfigCache 的作用</h3>
     * <pre>
     * 为什么需要缓存？
     *
     * 问题：ReferenceConfig 的创建成本很高
     * - 解析配置
     * - 创建代理对象
     * - 建立网络连接
     * - 注册监听器
     * 每次调用都创建新实例会导致性能问题
     *
     * 解决方案：ReferenceConfigCache
     * - 根据 (interface, version, group) 作为 key 缓存
     * - 相同配置的调用共享同一个代理实例
     * - 显著降低开销（从 ~50ms 降至 ~1ms）
     *
     * 注意：使用完后必须 destroy()，否则内存泄漏
     * </pre>
     *
     * <h3>适用场景举例</h3>
     * <pre>
     * 场景1：API 网关
     * 前端请求 → 网关根据 URL 路径动态决定调用哪个服务
     * /api/user/login → UserService.userLogin()
     * /api/order/create → OrderService.createOrder()
     * 网关不需要引入所有服务的接口 JAR
     *
     * 场景2：测试平台
     * 用户在界面上填写：
     * - 接口名：com.zhouByte.api.UserService
     * - 方法名：userLogin
     * - 参数：["admin", "123456"]
     * 平台自动构造 $invoke 调用并展示结果
     *
     * 场景3：服务降级
     * 当某个服务不可用时，网关切换到 Mock 版本
     * Mock 服务也是泛化实现，返回默认数据
     * </pre>
     *
     * @param username 用户名
     * @param password 密码
     * @return String 包含泛化调用的完整过程和返回结果
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/generic/{username}/{password}")
    public String testGenericCall(@PathVariable String username, @PathVariable String password) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface("com.zhouByte.api.UserService");
        referenceConfig.setVersion("1.0.0");
        referenceConfig.setGroup("generic");
        referenceConfig.setGeneric("true");

        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService = cache.get(referenceConfig);

        try {
            Object result = genericService.$invoke(
                    "userLogin",
                    new String[]{"java.lang.String", "java.lang.String"},
                    new Object[]{username, password}
            );

            Map<String, Object> resultMap = (Map<String, Object>) result;

            return """
                    [泛化调用演示]
                    
                    ✨ 特点:
                    - 无需依赖 UserService 接口 JAR 包
                    - 动态指定接口名、方法名、参数类型
                    - 返回值类型为 Object（通常是 Map）
                    
                    🎯 适用场景:
                    - API 网关统一转发
                    - 测试平台动态调用
                    - 服务Mock平台
                    - 泛型HTTP转RPC网关
                    
                    📋 调用信息:
                    - Interface: com.zhouByte.api.UserService
                    - Method: userLogin
                    - Parameters: [String, String]
                    
                    📦 返回结果 (Map):
                    """ + formatMap(resultMap);

        } finally {
            cache.destroy(referenceConfig);
        }
    }

    /**
     * 格式化 Map 为易读的字符串 - 辅助方法
     *
     * <h3>输出格式</h3>
     * <pre>
     *   key1: value1
     *   key2: value2
     *   key3: value3
     * </pre>
     *
     * @param map 待格式化的 Map 对象
     * @return String 格式化后的键值对字符串
     */
    private String formatMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
