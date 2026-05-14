package com.zhouByte.observability;

import org.apache.dubbo.config.annotation.DubboService;

/**
 * 指标查询服务实现 - 提供监控指标查询功能
 * 
 * Dubbo 服务暴露说明:
 * @DubboService 配置:
 *   - interfaceClass = MetricsQueryService.class: 指定暴露的服务接口
 *   - group = "observability": 服务分组，与 Consumer 端匹配
 *   - version = "1.0.0": 服务版本号，与 Consumer 端匹配
 * 
 * 功能说明:
 *   - 委托调用 MetricsFilter 的静态方法获取/重置指标
 *   - MetricsFilter 负责收集所有服务的性能指标数据
 */
@DubboService(
        interfaceClass = MetricsQueryService.class,
        group = "observability",
        version = "1.0.0"
)
public class MetricsQueryServiceImpl implements MetricsQueryService {

    @Override
    public String getMetricsReport() {
        return MetricsFilter.getMetricsReport();
    }

    @Override
    public void resetMetrics() {
        MetricsFilter.resetMetrics();
    }
}
