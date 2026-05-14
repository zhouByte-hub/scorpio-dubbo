package com.zhouByte.observability;

/**
 * 指标查询服务接口 - Dubbo RPC 服务契约
 * 
 * Dubbo 接口说明:
 *   - 此接口定义了查询 Dubbo 服务监控指标的契约
 *   - Provider 实现此接口并暴露为远程服务
 *   - Consumer 通过此接口远程获取 Provider 端的监控数据
 * 
 * 服务方法:
 *   - getMetricsReport: 获取格式化的监控指标报告
 *     参数: 无
 *     返回值: String 类型，包含请求数、成功率、响应时间等指标
 *   - resetMetrics: 重置所有监控指标
 *     参数: 无
 *     返回值: void
 */
public interface MetricsQueryService {

    /**
     * 获取格式化的监控指标报告
     * @return String 监控指标报告
     */
    String getMetricsReport();

    /**
     * 重置所有监控指标
     */
    void resetMetrics();
}
