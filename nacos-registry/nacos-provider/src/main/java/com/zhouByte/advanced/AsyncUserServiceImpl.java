package com.zhouByte.advanced;

import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步用户服务实现类 - 演示 Dubbo Provider 端的异步编程最佳实践
 *
 * <h2>核心职责</h2>
 * 实现 AsyncUserService 接口，展示如何在 Provider 端正确地返回 CompletableFuture，
 * 以及如何管理异步任务的执行线程池。
 *
 * <h2>关键技术点</h2>
 * <ol>
 *   <li><b>线程池隔离</b> - 使用独立的 ExecutorService，避免阻塞 Dubbo IO 线程</li>
 *   <li><b>异步返回</b> - 方法立即返回 Future，实际计算在后台进行</li>
 *   <li><b>异常传播</b> - 通过 Future.completeExceptionally() 传递错误</li>
 *   <li><b>资源管理</b> - 合理配置线程池大小和生命周期</li>
 * </ol>
 *
 * <h2>为什么不能阻塞 Dubbo 线程？</h2>
 * <pre>
 * Dubbo 默认线程模型：
 *
 * Consumer                          Provider
 * ┌──────────┐    RPC Request     ┌─────────────┐
 * │ Business │ ─────────────────→ │ IO Thread   │ ← 固定数量（如 200）
 * │  Thread  │                    │ (处理请求)   │
 * └──────────┘                    └──────┬──────┘
 *                                        │
 *                                 ┌──────▼──────┐
 *                                 │ Business    │ ← 如果在这里 sleep/block
 *                                 │ Logic       │   IO 线程被占用！
 *                                 └─────────────┘   其他请求排队等待...
 *
 * 问题：
 * - 如果所有 IO 线程都被阻塞 → 新请求无法处理 → 服务假死
 * - 典型症状：dubbo.threadpool.rejected.execution 异常
 *
 * 解决方案（本类采用的方式）：
 * - IO 线程只负责接收请求和返回 Future
 * - 实际业务逻辑交给自定义线程池执行
 * - IO 线程立即释放，可以服务更多请求
 * </pre>
 *
 * <h2>线程池配置说明</h2>
 * <pre>
 * 当前配置: Executors.newFixedThreadPool(10)
 *
 * 含义：
 * - 核心线程数 = 最大线程数 = 10
 * - 使用无界队列（LinkedBlockingQueue）
 * - 当 10 个线程都在忙时，新任务进入队列等待
 * - 适用于 CPU 密集型任务（避免过多上下文切换）
 *
 * 生产环境优化建议：
 * - IO 密集型: 线程数 = CPU核心数 × 2 (本例)
 * - IO 密集型: 线程数 = CPU核心数 / (1 - 阻塞系数)
 *   例如：8核CPU，阻塞系数0.8 → 8/(1-0.8) = 40 线程
 *
 * 更优选择：使用 ThreadPoolExecutor 自定义参数
 * new ThreadPoolExecutor(
 *     corePoolSize,      // 核心线程数
 *     maximumPoolSize,   // 最大线程数
 *     keepAliveTime,     // 空闲线程存活时间
 *     TimeUnit.SECONDS,
 *     new ArrayBlockingQueue<>(1000),  // 有界队列，防止 OOM
 *     new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
 * );
 * </pre>
 *
 * <h2>Dubbo 配置</h2>
 * <pre>
 * @DubboService 注解关键属性：
 * - interfaceClass: 指定实现的接口（必须）
 * - group: 服务分组，用于区分不同环境/版本
 * - version: 服务版本号，支持灰度发布
 *
 * Consumer 端引用配置：
 * @DubboReference(
 *     interfaceClass = AsyncUserService.class,
 *     group = "advanced",
 *     version = "1.0.0"
 * )
 * private AsyncUserService asyncUserService;
 * </pre>
 *
 * <h2>监控指标</h2>
 * <ul>
 *   <li>线程池活跃线程数: executorService.getActiveCount()</li>
 *   <li>队列积压任务数: executorService.getQueue().size()</li>
 *   <li>已完成任务总数: executorService.getCompletedTaskCount()</li>
 *   <li>建议将这些指标接入 Prometheus + Grafana</li>
 * </ul>
 *
 * @author zhouByte
 * @version 1.0.0
 * @see AsyncUserService
 * @see java.util.concurrent.CompletableFuture
 */
@DubboService(
        interfaceClass = AsyncUserService.class,
        group = "advanced",
        version = "1.0.0"
)
public class AsyncUserServiceImpl implements AsyncUserService {

    /**
     * 日志记录器 - 记录异步操作的执行状态和性能数据
     *
     * <p>日志输出内容：
     * <ul>
     *   <li>请求到达时间戳</li>
     *   <li>线程池使用情况</li>
     *   <li>任务执行耗时</li>
     *   <li>异常堆栈信息</li>
     * </ul>
     */
    private static final Logger logger = LoggerFactory.getLogger(AsyncUserServiceImpl.class);
    
    /**
     * 业务逻辑专用线程池 - 用于执行耗时的异步任务
     *
     * <h3>为什么需要独立线程池？</h3>
     * <pre>
     * Dubbo 的 IO 线程（默认 200 个）负责：
     * 1. 反序列化请求参数
     * 2. 调用 Service 方法
     * 3. 序列化返回值
     * 4. 发送响应给 Consumer
     *
     * 如果 Service 方法内部有耗时操作（DB查询、HTTP调用、复杂计算），
     * 会长时间占用 IO 线程，导致：
     * - 新请求无法被处理（线程耗尽）
     * - 响应时间急剧上升
     * - 甚至触发线程池拒绝策略
     *
     * 解决方案：
     * Service 方法立即返回 CompletableFuture
     * 将耗时任务提交到这个独立的线程池
     * IO 线程立即释放去服务其他请求
     * </pre>
     *
     * <h3>FixedThreadPool(10) 特性</h3>
     * <ul>
     *   <li>固定 10 个线程，不会自动增长</li>
     *   <li>使用无界 LinkedBlockingQueue 存储等待的任务</li>
     *   <li>空闲线程不会被回收（keepAliveTime=0）</li>
     *   <li>适合任务量可控的场景</li>
     * </ul>
     *
     * <h3>生产环境注意事项</h3>
     * <ul>
     *   <li>无界队列可能导致 OOM（如果任务产生速度 &gt; 消费速度）</li>
     *   <li>建议改用有界队列 + 合适的拒绝策略</li>
     *   <li>应用关闭时应调用 executorService.shutdown()</li>
     *   <li>可通过 Spring 的 @PreDestroy 进行优雅停机</li>
     * </ul>
     *
     * @see Executors#newFixedThreadPool(int)
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 异步登录实现 - 演示 CompletableFuture 的正确使用方式
     *
     * <h3>执行流程</h3>
     * <pre>
     * 1. [IO Thread] 接收到请求，记录日志
     * 2. [IO Thread] 创建 CompletableFuture 并立即返回给 Dubbo 框架
     * 3. [IO Thread] 释放，可以去处理下一个请求  ← 关键！
     * 4. [Pool-Thread-1] 从线程池获取线程执行 supplyAsync 的 lambda
     * 5. [Pool-Thread-1] 模拟耗时操作（sleep 100-300ms）
     * 6. [Pool-Thread-1] 构建结果字符串
     * 7. [Pool-Thread-1] CompletableFuture.complete(result) 自动触发
     * 8. [Consumer] 收到回调通知，Future 变为 completed 状态
     * 9. [Consumer] 调用 .get() 或 .thenAccept() 获取结果
     * </pre>
     *
     * <h3>CompletableFuture.supplyAsync() 参数说明</h3>
     * <ul>
     *   <li><b>Supplier&lt;U&gt; supplier</b> - 无参但有返回值的 lambda</li>
     *   <li><b>Executor executor</b> - 指定执行器（可选，默认 ForkJoinPool）</li>
     * </ul>
     *
     * <h3>异常处理机制</h3>
     * <pre>
     * 如果 lambda 内部抛出异常：
     * 1. 异常被捕获但不会立即抛出
     * 2. CompletableFuture 进入异常状态（exceptionally）
     * 3. Consumer 调用 .get() 时会抛出 ExecutionException
     * 4. 或者通过 .exceptionally(fn) 恢复
     *
     * 示例：
     * return CompletableFuture.supplyAsync(() -> {
     *     if (password == null) {
     *         throw new IllegalArgumentException("密码不能为空");
     *     }
     *     return doLogin(username, password);
     * }, executorService);
     * // Consumer 端：
     * future.exceptionally(ex -> {
     *     log.error("登录失败", ex);
     *     return "默认错误信息";
     * });
     * </pre>
     *
     * <h3>InterruptedException 处理</h3>
     * <p>当线程在 sleep/wait 期间被中断时：
     * <ul>
     *   <li>清除中断标志位（Thread.interrupted() 会这样做）</li>
     *   <li>重新设置中断标志（Thread.currentThread().interrupt()）</li>
     *   <li>让上层决定如何处理（通常应该停止当前任务）</li>
     * </ul>
     *
     * @param username 用户名
     * @param password 密码
     * @return CompletableFuture<String> 包含登录详情的异步结果
     */
    @Override
    public CompletableFuture<String> asyncLogin(String username, String password) {
        logger.info("[异步服务] 接收到异步登录请求 | 用户名={}", username);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep((long) (Math.random() * 200 + 100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            logger.info("[异步服务] 异步处理完成 | 用户名={}", username);

            return "[ASYNC] " + username + " 异步登录成功\n"
                    + "- 使用线程池: FixedThreadPool(10)\n"
                    + "- 模拟耗时: 100-300ms\n"
                    + "- 返回类型: CompletableFuture<String>";
        }, executorService);
    }

    /**
     * 同步登录实现 - 用于对比演示的传统阻塞式调用
     *
     * <h3>与异步方法的区别</h3>
     * <pre>
     * 同步方法执行时序：
     * Time → │████████████████████│
     *        ↑ IO线程阻塞在这里    ↑ 返回结果
     *        （无法服务其他请求）
     *
     * 异步方法执行时序：
     * Time → │░░░│░░░░░░░░░░░░░░░░│
     *        ↑ 立即返回Future  ↑ 后台完成后回调
     *        （IO线程已释放）
     * </pre>
     *
     * <h3>何时使用同步方法？</h3>
     * <ul>
     *   <li>简单的 CRUD 操作（耗时 &lt; 10ms）</li>
     *   <li>调用链中的最后一步（后续无依赖操作）</li>
     *   <li>团队对 CompletableFuture 不熟悉</li>
     *   <li>需要简化代码以降低维护成本</li>
     * </ul>
     *
     * <h3>性能影响评估</h3>
     * <p>假设单次 RPC 耗时 200ms，线程池 200 个：
     * <ul>
     *   <li>同步模式最大 QPS = 200 / 0.2 = 1000 req/s</li>
     *   <li>异步模式理论 QPS 可达数万（仅受限于网络带宽）</li>
     *   <li>实际差异取决于业务逻辑的 IO/CPU 占比</li>
     * </ul>
     *
     * @param username 用户名
     * @param password 密码
     * @return String 包含登录信息的同步结果
     */
    @Override
    public String syncLogin(String username, String password) {
        logger.info("[同步服务] 接收到同步登录请求 | 用户名={}", username);

        try {
            Thread.sleep((long) (Math.random() * 200 + 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("[同步服务] 同步处理完成 | 用户名={}", username);

        return "[SYNC] " + username + " 同步登录成功\n"
                + "- 阻塞当前线程\n"
                + "- 模拟耗时: 100-300ms";
    }
}
