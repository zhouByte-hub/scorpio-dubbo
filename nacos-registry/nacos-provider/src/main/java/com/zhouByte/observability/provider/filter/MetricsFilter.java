package com.zhouByte.observability.provider.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Activate(group = {CommonConstants.PROVIDER}, order = -10000)
public class MetricsFilter implements Filter {

    private static final Map<String, Long> requestCount = new ConcurrentHashMap<>();
    private static final Map<String, Long> successCount = new ConcurrentHashMap<>();
    private static final Map<String, Long> failureCount = new ConcurrentHashMap<>();
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

    public static void resetMetrics() {
        requestCount.clear();
        successCount.clear();
        failureCount.clear();
        totalResponseTime.clear();
    }
}
