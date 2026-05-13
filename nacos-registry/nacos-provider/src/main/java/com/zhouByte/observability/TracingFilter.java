package com.zhouByte.observability;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 分布式链路追踪过滤器
 * 生成/传递 traceId 和 spanId，实现跨服务的请求链路追踪
 * 仅在 Provider 端生效
 * 
 * Dubbo Filter 扩展机制说明:
 * @Activate 注解:
 *   - group = {CommonConstants.PROVIDER}: 仅在 Provider 端激活
 *   - order = -11000: 执行顺序，比 MetricsFilter(-10000) 更早执行
 * 
 * 链路追踪核心概念:
 *   - traceId: 全局唯一的请求追踪ID，贯穿整个请求链路
 *   - spanId: 当前节点的标识，用于区分链路中的不同服务节点
 *   - parentSpanId: 父节点标识，用于构建调用链树
 * 
 * RpcContext 传递机制:
 *   - invocation.getAttachment(key): 从上游获取传递的附件信息
 *   - RpcContext.getServerContext().setAttachment(key, value): 设置附件传递给下游
 */
@Activate(group = {CommonConstants.PROVIDER}, order = -11000)
public class TracingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TracingFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        // 从上游获取 traceId，若无则生成新的
        String traceId = invocation.getAttachment("traceId");
        String parentSpanId = invocation.getAttachment("spanId");

        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        if (parentSpanId == null || parentSpanId.isEmpty()) {
            parentSpanId = "0";
        }

        String currentSpanId = generateSpanId();

        // 将追踪信息写入 ServerContext，传递给下游
        RpcContext.getServerContext().setAttachment("traceId", traceId);
        RpcContext.getServerContext().setAttachment("spanId", currentSpanId);

        logger.info("[TRACE] 开始 | traceId={} | parentSpanId={} | spanId={} | service={} | method={}",
                traceId, parentSpanId, currentSpanId,
                invoker.getInterface().getSimpleName(),
                invocation.getMethodName());

        long startTime = System.currentTimeMillis();

        try {
            Result result = invoker.invoke(invocation);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("[TRACE] 完成 | traceId={} | spanId={} | 耗时={}ms | 状态=成功",
                    traceId, currentSpanId, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            logger.error("[TRACE] 失败 | traceId={} | spanId={} | 耗时={}ms | 错误={}",
                    traceId, currentSpanId, duration, e.getMessage());

            throw e;
        }
    }

    /** 生成 16 位 TraceId */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /** 生成 8 位 SpanId */
    private String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
