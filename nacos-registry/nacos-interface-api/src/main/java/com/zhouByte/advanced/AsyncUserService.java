package com.zhouByte.advanced;

import java.util.concurrent.CompletableFuture;

/**
 * 异步用户服务接口 - 定义支持异步调用的 Dubbo 服务契约
 *
 * <h2>接口定位</h2>
 * 该接口是 Dubbo 异步编程模型的核心定义，展示了如何将传统的同步 RPC 调用
 * 升级为基于 CompletableFuture 的异步非阻塞模式。
 *
 * <h2>设计理念</h2>
 * <ul>
 *   <li><b>双模式支持</b> - 同时提供同步和异步两种调用方式，便于对比和迁移</li>
 *   <li><b>向后兼容</b> - 保留 syncLogin 方法，确保现有代码无需修改即可运行</li>
 *   <li><b>渐进式演进</b> - 允许开发者逐步从同步迁移到异步，降低改造风险</li>
 * </ul>
 *
 * <h2>异步调用优势</h2>
 * <table border="1">
 *   <tr><th>维度</th><th>同步调用</th><th>异步调用</th></tr>
 *   <tr><td>线程占用</td><td>阻塞等待期间线程无法释放</td><td>提交后立即释放，可处理其他请求</td></tr>
 *   <tr><td>并发能力</td><td>受限于线程池大小</td><td>理论上无限（取决于资源）</td></tr>
 *   <tr><td>响应延迟</td><td>= RPC 耗时</td><td>&lt;= RPC 耗时（可并行处理多个调用）</td></tr>
 *   <tr><td>代码复杂度</td><td>简单直观</td><td>需要理解 CompletableFuture API</td></tr>
 * </table>
 *
 * <h2>CompletableFuture 核心方法速查</h2>
 * <pre>
 * // 基础操作
 * future.get()                    ← 阻塞获取结果
 * future.get(timeout, unit)       ← 带超时的阻塞获取
 * future.join()                   ← 阻塞获取（不抛检查异常）
 *
 * // 转换操作（返回新 Future）
 * future.thenApply(fn)            ← 同步转换结果
 * future.thenApplyAsync(fn)       ← 异步转换结果
 * future.thenCompose(fn)          ← 扁平化映射（Future 嵌套）
 * future.handle(fn)               ← 处理成功/失败
 *
 * // 消费操作（无返回值）
 * future.thenAccept(consumer)     ← 消费结果
 * future.thenRun(runnable)        ← 完成后执行动作
 *
 * // 组合操作
 * future1.combineAsync(future2, fn)  ← 合并两个 Future
 * future.acceptEither(other, consumer)← 取先完成的那个
 * CompletableFuture.allOf(futures)   ← 等待所有完成
 * CompletableFuture.anyOf(futures)   ← 等待任一完成
 * </pre>
 *
 * <h2>使用示例</h2>
 * <pre>
 * // Consumer 端调用异步方法
 * @DubboReference
 * private AsyncUserService asyncUserService;
 *
 * public void example() {
 *     // 方式1：阻塞等待结果（不推荐，失去异步意义）
 *     String result = asyncUserService.asyncLogin("user", "pwd").get();
 *
 *     // 方式2：链式处理（推荐）
 *     asyncUserService.asyncLogin("user", "pwd")
 *         .thenAccept(loginResult -> {
 *             System.out.println("登录成功: " + loginResult);
 *         })
 *         .exceptionally(ex -> {
 *             System.err.println("登录失败: " + ex.getMessage());
 *             return null;
 *         });
 *
 *     // 方式3：带超时控制
 *     try {
 *         String result = asyncUserService.asyncLogin("user", "pwd")
 *             .get(5, TimeUnit.SECONDS);
 *     } catch (TimeoutException e) {
 *         // 处理超时
 *     }
 * }
 * </pre>
 *
 * <h2>Provider 实现要求</h2>
 * <ul>
 *   <li>返回值必须是 CompletableFuture&lt;T&gt; 类型</li>
 *   <li>建议使用独立的线程池执行耗时操作，避免阻塞 Dubbo IO 线程</li>
 *   <li>异常应通过 CompletableFuture.completeExceptionally() 传递</li>
 *   <li>不要在 Provider 端直接调用 .get()，否则会退化为同步</li>
 * </ul>
 *
 * @author zhouByte
 * @version 1.0.0
 * @see java.util.concurrent.CompletableFuture
 * @see AsyncUserServiceImpl
 */
public interface AsyncUserService {

    /**
     * 异步登录方法 - 返回 CompletableFuture 的非阻塞 RPC 调用
     *
     * <h3>工作原理</h3>
     * <ol>
     *   <li>Consumer 调用此方法后立即获得 CompletableFuture 对象</li>
     *   <li>Dubbo 框架在底层建立 RPC 连接并发送请求</li>
     *   <li>Consumer 线程继续执行其他任务（非阻塞）</li>
     *   <li>Provider 在独立线程中处理业务逻辑</li>
     *   <li>处理完成后通过回调机制通知 Consumer</li>
     *   <li>Consumer 通过 Future.get() 或 thenAccept() 获取结果</li>
     * </ol>
     *
     * <h3>性能提升场景</h3>
     * <ul>
     *   <li><b>聚合网关</b> - 同时调用多个下游服务并合并结果</li>
     *   <li><b>批量处理</b> - 并行发送多个独立请求</li>
     *   <li><b>事件驱动</b> - 触发操作后不关心即时结果</li>
     *   <li><b>高并发系统</b> - 减少线程池压力，提升吞吐量</li>
     * </ul>
     *
     * <h3>与同步方法的区别</h3>
     * <pre>
     * 同步调用:
     * Consumer → [阻塞等待] → Provider 返回 → Consumer 继续执行
     *              ↑ 整个过程占用一个线程
     *
     * 异步调用:
     * Consumer → [立即返回 Future] → Consumer 继续执行其他任务
     *                ↓ (后台)
     *           Provider 处理完成 → 回调通知 Consumer
     *              ↑ Consumer 线程可复用
     * </pre>
     *
     * @param username 用户名，用于身份认证
     * @param password 用户密码，需要与数据库中的哈希值比对
     * @return CompletableFuture<String> 包含登录结果的异步对象
     *         <ul>
     *           <li>成功时：包含用户信息和 Token 的字符串</li>
     *           <li>失败时：通过 Future.exceptionally() 捕获异常</li>
     *         </ul>
     *
     * @throws RuntimeException 当认证失败或系统错误时（通过 Future 传递）
     *
     * @see #syncLogin(String, String) 对比的同步版本
     */
    CompletableFuture<String> asyncLogin(String username, String password);

    /**
     * 同步登录方法 - 传统的阻塞式 RPC 调用（用于对比演示）
     *
     * <h3>设计目的</h3>
     * 该方法主要用于：
     * <ul>
     *   <li>作为基线对比，展示同步和异步的性能差异</li>
     *   <li>提供向后兼容性，旧代码可直接使用</li>
     *   <li>帮助开发者理解两种模式的区别和适用场景</li>
     * </ul>
     *
     * <h3>调用特点</h3>
     * <ul>
     *   <li>调用线程会一直阻塞直到 Provider 返回结果</li>
     *   <li>在高并发场景下容易耗尽线程池资源</li>
     *   <li>代码逻辑简单直观，易于调试</li>
     *   <li>适合对延迟不敏感或需要顺序执行的流程</li>
     * </ul>
     *
     * <h3>何时选择同步？</h3>
     * <pre>
     * 适用场景：
     * ✅ 后续逻辑强依赖当前结果（A 的结果是 B 的输入）
     * ✅ 调用链较短（&lt; 3 层嵌套）
     * ✅ 团队对异步编程不够熟悉
     * ✅ 性能不是瓶颈（RPC 耗时 &lt; 10ms）
     *
     * 不适用场景：
     * ❌ 需要聚合多个独立服务的返回值
     * ❌ 高 QPS 场景（&gt; 1000 TPS）
     * ❌ 需要实现超时熔断机制
     * ❌ 前端需要快速响应（长轮询、WebSocket 推送等）
     * </pre>
     *
     * @param username 用户名
     * @param password 密码
     * @return String 登录结果的字符串表示
     *
     * @see #asyncLogin(String, String) 推荐的异步版本
     */
    String syncLogin(String username, String password);
}
