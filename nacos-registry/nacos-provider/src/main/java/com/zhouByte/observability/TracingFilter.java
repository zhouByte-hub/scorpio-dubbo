package com.zhouByte.observability;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Activate(group = {CommonConstants.PROVIDER}, order = -11000)
public class TracingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TracingFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        String traceId = RpcContext.getServerAttachment().get("traceId");
        String spanId = RpcContext.getServerAttachment().get("spanId");

        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        if (spanId == null || spanId.isEmpty()) {
            spanId = "0";
        }

        String currentSpanId = generateSpanId();

        RpcContext.getServerContext().setAttachment("traceId", traceId);
        RpcContext.getServerContext().setAttachment("spanId", currentSpanId);

        logger.info("[TRACE] 开始处理请求 | traceId={} | parentSpanId={} | currentSpanId={} | service={} | method={}",
                traceId, spanId, currentSpanId,
                invoker.getInterface().getSimpleName(),
                invocation.getMethodName());

        long startTime = System.currentTimeMillis();

        try {
            Result result = invoker.invoke(invocation);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("[TRACE] 请求处理完成 | traceId={} | spanId={} | 耗时={}ms | 状态=成功",
                    traceId, currentSpanId, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            logger.error("[TRACE] 请求处理失败 | traceId={} | spanId={} | 耗时={}ms | 错误={}",
                    traceId, currentSpanId, duration, e.getMessage());

            throw e;
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
