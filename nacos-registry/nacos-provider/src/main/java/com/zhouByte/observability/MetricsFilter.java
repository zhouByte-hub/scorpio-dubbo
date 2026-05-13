package com.zhouByte.observability;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能指标收集过滤器
 * 采集每个方法的请求数、成功/失败数、平均响应时间等指标
 * 仅在 Provider 端生效
 * 
 * Dubbo Filter 扩展机制说明:
 * @Activate 注解:
 *   作用: 声明该过滤器在什么条件下自动激活
 *   核心参数:
 *     - group: 指定激活的节点类型
 *       - CommonConstants.PROVIDER: 仅在服务提供者端激活
 *       - CommonConstants.CONSUMER: 仅在服务消费者端激活
 *       - 可同时指定多个: {CommonConstants.PROVIDER, CommonConstants.CONSUMER}
 *     - order: 过滤器执行顺序，数值越小越先执行
 *       - 本过滤器 order=-10000，表示较早执行
 *       - 用于控制过滤器链的执行顺序
 * 
 * Filter 接口:
 *   - invoke(Invoker<?> invoker, Invocation invocation): 过滤器核心方法
 *     参数说明:
 *       - invoker: 服务调用器，包含服务接口、URL、配置等信息
 *       - invocation: 调用上下文，包含方法名、参数、附件等信息
 *     返回值: Result 对象，包含调用结果或异常信息
 * 
 * 过滤器注册:
 *   在 META-INF/dubbo/org.apache.dubbo.rpc.Filter 文件中注册:
 *   metricsFilter=com.zhouByte.observability.MetricsFilter
 */
@Activate(group = {CommonConstants.PROVIDER}, order = -10000)
public class MetricsFilter implements Filter {

    /** 方法级请求计数 */
    private static final Map<String, Long> requestCount = new ConcurrentHashMap<>();
    /** 方法级成功计数 */
    private static final Map<String, Long> successCount = new ConcurrentHashMap<>();
    /** 方法级失败计数 */
    private static final Map<String, Long> failureCount = new ConcurrentHashMap<>();
    /** 方法级累计响应时间（ms） */
    private static final Map<String, Long> totalResponseTime = new ConcurrentHashMap<>();

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

    /** 生成格式化的监控指标报告 */
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

    /** 重置所有监控指标 */
    public static void resetMetrics() {
        requestCount.clear();
        successCount.clear();
        failureCount.clear();
        totalResponseTime.clear();
    }
}
