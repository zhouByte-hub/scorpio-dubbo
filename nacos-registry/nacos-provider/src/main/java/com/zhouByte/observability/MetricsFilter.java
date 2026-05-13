package com.zhouByte.observability;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能指标收集过滤器 - Dubbo 服务监控核心组件
 *
 * <h2>功能概述</h2>
 * 该过滤器实现了 Dubbo 服务的完整性能指标采集体系，用于实时监控服务运行状态。
 * 通过在请求处理过程中拦截和统计关键性能数据，为运维人员提供决策依据。
 *
 * <h2>核心能力</h2>
 * <ul>
 *   <li><b>流量统计</b> - 精确记录每个方法的调用次数、成功/失败分布</li>
 *   <li><b>性能分析</b> - 计算平均响应时间、总耗时等性能指标</li>
 *   <li><b>健康评估</b> - 通过成功率判断服务健康状态</li>
 *   <li><b>容量规划</b> - 基于历史数据进行系统容量预测</li>
 * </ul>
 *
 * <h2>技术实现</h2>
 * <ol>
 *   <li><b>线程安全设计</b> - 使用 ConcurrentHashMap 保证高并发下的数据一致性</li>
 *   <li><b>低侵入性</b> - 基于 Dubbo Filter SPI 机制，对业务代码零侵入</li>
 *   <li><b>实时计算</b> - 每次请求完成后立即更新统计数据</li>
 *   <li><b>异常容错</b> - 正常处理 RpcException 等异常情况</li>
 * </ol>
 *
 * <h2>使用场景</h2>
 * <pre>
 * 适用场景：
 * - 生产环境性能监控
 * - 容量规划和扩容决策
 * - 故障排查和根因分析
 * - SLA（服务水平协议）合规性检查
 * </pre>
 *
 * <h2>Dubbo 配置示例</h2>
 * <pre>
 * # 在 application.yaml 中配置
 * dubbo:
 *   provider:
 *     filter: metricsFilter
 *
 * # 或者在 @DubboService 注解中指定
 * @DubboService(filter = "metricsFilter")
 * </pre>
 *
 * <h2>指标说明</h2>
 * <table border="1">
 *   <tr><th>指标名称</th><th>含义</th><th>用途</th></tr>
 *   <tr><td>总请求数</td><td>方法被调用的总次数</td><td>衡量服务热度</td></tr>
 *   <tr><td>成功数</td><td>成功完成的请求数</td><td>评估服务稳定性</td></tr>
 *   <tr><td>失败数</td><td>发生异常的请求数</td><td>发现潜在问题</td></tr>
 *   <tr><td>成功率</td><td>成功数/总数×100%</td><td>SLA 合规性指标</td></tr>
 *   <tr><td>平均响应时间</td><td>总耗时/请求数</td><td>性能基准线</td></tr>
 * </table>
 *
 * <h2>注意事项</h2>
 * <ul>
 *   <li>该过滤器仅在 Provider 端生效（@Activate group = PROVIDER）</li>
 *   <li>执行优先级为 -10000，确保在其他业务逻辑之前运行</li>
 *   <li>统计数据存储在内存中，应用重启后会丢失</li>
 *   <li>生产环境建议配合 Prometheus + Grafana 等工具进行持久化</li>
 * </ul>
 *
 * @author zhouByte
 * @version 1.0.0
 * @see org.apache.dubbo.rpc.Filter
 * @see ConcurrentHashMap
 */
@Activate(group = {CommonConstants.PROVIDER}, order = -10000)
public class MetricsFilter implements Filter {

    /**
     * 方法级别的请求计数器
     *
     * <p>Key 格式: "接口名.方法名" (例如: UserService.login)
     * <br>Value: 该方法累计接收的请求总数
     *
     * <p><b>线程安全保证:</b> 使用 ConcurrentHashMap 的 merge 操作，
     * 该操作是原子性的，即使在多线程环境下也能保证数据准确性。
     *
     * <p><b>内存占用:</b> 每个活跃方法占用约 100 字节（key + Long 对象）
     */
    private static final Map<String, Long> requestCount = new ConcurrentHashMap<>();

    /**
     * 方法级别的成功响应计数器
     *
     * <p>仅记录正常完成（无异常）的请求次数。
     * <br>用于计算成功率和评估服务质量。
     *
     * <p><b>统计口径:</b> Result.hasException() == false 的请求
     */
    private static final Map<String, Long> successCount = new ConcurrentHashMap<>();

    /**
     * 方法级别的失败计数器
     *
     * <p>记录所有产生异常的请求，包括：
     * <ul>
     *   <li>业务逻辑抛出的异常</li>
     *   <li>RpcException（网络超时、序列化失败等）</li>
     *   <li>其他 RuntimeException</li>
     * </ul>
     *
     * <p><b>告警阈值建议:</b> 失败率 > 1% 时触发告警
     */
    private static final Map<String, Long> failureCount = new ConcurrentHashMap<>();

    /**
     * 方法级别的累计响应时间（毫秒）
     *
     * <p>存储所有请求的处理时间总和。
     * <br>通过 总耗时 / 请求数 计算平均响应时间。
     *
     * <p><b>精度说明:</b> 使用 System.currentTimeMillis()，
     * 精度约为 15ms（取决于操作系统），对于毫秒级监控足够使用。
     * 如需微秒级精度，可改用 System.nanoTime()。
     */
    private static final Map<String, Long> totalResponseTime = new ConcurrentHashMap<>();

    /**
     * 过滤器核心拦截方法 - 执行指标采集逻辑
     *
     * <h3>执行流程</h3>
     * <ol>
     *   <li><b>生成方法标识符</b> - 构建 "接口名.方法名" 格式的唯一 key</li>
     *   <li><b>记录开始时间</b> - 获取当前系统时间戳作为起点</li>
     *   <li><b>执行后续链路</b> - 调用 invoker.invoke() 触发实际业务处理</li>
     *   <li><b>计算耗时</b> - 用当前时间减去开始时间得到处理时长</li>
     *   <li><b>更新统计指标</b> - 根据执行结果更新对应的计数器</li>
     *   <li><b>返回结果</b> - 将原始结果透传给调用方</li>
     * </ol>
     *
     * <h3>异常处理策略</h3>
     * <ul>
     *   <li>捕获 RpcException 并记录失败指标后重新抛出</li>
     *   <li>确保即使发生异常，统计数据依然准确</li>
     *   <li>不影响原有异常传播机制</li>
     * </ul>
     *
     * @param invoker 服务调用器，包含目标服务的元信息（接口名、方法名等）
     * @param invocation 调用上下文，包含方法参数、附加属性等信息
     * @return Result 调用结果，包含返回值或异常信息
     * @throws RpcException 当远程调用发生错误时抛出
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        String methodKey = invoker.getInterface().getSimpleName() + "." + invocation.getMethodName();
        long startTime = System.currentTimeMillis();

        try {
            Result result = invoker.invoke(invocation);
            long costTime = System.currentTimeMillis() - startTime;

            if (result.hasException()) {
                failureCount.merge(methodKey, 1L, Long::sum);
            } else {
                successCount.merge(methodKey, 1L, Long::sum);
            }

            requestCount.merge(methodKey, 1L, Long::sum);
            totalResponseTime.merge(methodKey, costTime, Long::sum);

            return result;
        } catch (RpcException e) {
            long costTime = System.currentTimeMillis() - startTime;
            
            requestCount.merge(methodKey, 1L, Long::sum);
            failureCount.merge(methodKey, 1L, Long::sum);
            totalResponseTime.merge(methodKey, costTime, Long::sum);
            
            throw e;
        }
    }

    /**
     * 生成格式化的监控指标报告
     *
     * <h3>报告内容</h3>
     * <ul>
     *   <li>每个已调用方法的详细统计信息</li>
     *   <li>包括：总请求数、成功数、失败数、成功率、平均响应时间、总耗时</li>
     * </ul>
     *
     * <h3>输出格式示例</h3>
     * <pre>
     * ========== Dubbo 监控指标报告 ==========
     *
     * 方法: UserService.login
     *   总请求数: 150
     *   成功数: 148
     *   失败数: 2
     *   成功率: 98.67%
     *   平均响应时间: 45.23 ms
     *   总耗时: 6784 ms
     * </pre>
     *
     * <h3>使用方式</h3>
     * 可通过 REST API 或管理后台定时调用此方法获取最新指标：
     * <pre>
     * // 在 Controller 中调用
     * String report = MetricsFilter.getMetricsReport();
     * return ResponseEntity.ok(report);
     * </pre>
     *
     * @return 格式化的指标报告字符串，适合直接展示或写入日志
     */
    public static String getMetricsReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n========== Dubbo 监控指标报告 ==========\n");
        
        for (String key : requestCount.keySet()) {
            long requests = requestCount.getOrDefault(key, 0L);
            long successes = successCount.getOrDefault(key, 0L);
            long failures = failureCount.getOrDefault(key, 0L);
            long totalTime = totalResponseTime.getOrDefault(key, 0L);
            double avgTime = requests > 0 ? (double) totalTime / requests : 0;
            double successRate = requests > 0 ? (double) successes / requests * 100 : 0;
            
            report.append(String.format("\n方法: %s\n", key));
            report.append(String.format("  总请求数: %d\n", requests));
            report.append(String.format("  成功数: %d\n", successes));
            report.append(String.format("  失败数: %d\n", failures));
            report.append(String.format("  成功率: %.2f%%\n", successRate));
            report.append(String.format("  平均响应时间: %.2f ms\n", avgTime));
            report.append(String.format("  总耗时: %d ms\n", totalTime));
        }
        
        return report.toString();
    }

    /**
     * 重置所有监控指标数据
     *
     * <h3>使用场景</h3>
     * <ul>
     *   <li>测试环境：每次测试前清空历史数据，确保测试结果准确</li>
     *   <li>生产环境：定期重置避免内存持续增长（配合定时任务使用）</li>
     *   <li>故障恢复：问题解决后重置指标，重新开始统计周期</li>
     * </ul>
     *
     * <h3>注意事项</h3>
     * <ul>
     *   <li>此操作不可逆，清空后历史数据无法恢复</li>
     *   <li>多线程环境下可能存在短暂的数据不一致（可接受）</li>
     *   <li>生产环境建议添加权限控制，防止误操作</li>
     * </ul>
     *
     * <h3>调用示例</h3>
     * <pre>
     * // 通过 HTTP 接口触发重置
     * POST /observability/metrics/reset
     *
     * // 或在代码中直接调用
     * MetricsFilter.resetMetrics();
     * </pre>
     */
    public static void resetMetrics() {
        requestCount.clear();
        successCount.clear();
        failureCount.clear();
        totalResponseTime.clear();
    }
}
