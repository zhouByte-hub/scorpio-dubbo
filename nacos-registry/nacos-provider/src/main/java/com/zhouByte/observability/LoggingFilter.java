package com.zhouByte.observability;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 结构化日志记录过滤器 - 统一记录 Dubbo 调用的请求参数、返回值、异常信息
 */
@Activate(group = {CommonConstants.PROVIDER}, order = -12000)
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        String interfaceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        Object[] args = invocation.getArguments();

        logger.info("[DUBBO-LOG] 接收到请求 | 服务={} | 方法={} | 参数值={}",
                interfaceName, methodName,
                args != null ? java.util.Arrays.toString(args) : "null");

        long startTime = System.currentTimeMillis();

        try {
            Result result = invoker.invoke(invocation);
            long costTime = System.currentTimeMillis() - startTime;

            logger.info("[DUBBO-LOG] 请求完成 | 服务={} | 方法={} | 耗时={}ms",
                    interfaceName, methodName, costTime);

            return result;
        } catch (RpcException e) {
            long costTime = System.currentTimeMillis() - startTime;

            logger.error("[DUBBO-LOG] 请求异常 | 服务={} | 方法={} | 耗时={}ms | 错误={}",
                    interfaceName, methodName, costTime, e.getMessage(), e);

            throw e;
        }
    }
}
