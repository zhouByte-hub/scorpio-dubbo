package com.zhouByte.observability;

import org.apache.dubbo.config.annotation.DubboService;

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
