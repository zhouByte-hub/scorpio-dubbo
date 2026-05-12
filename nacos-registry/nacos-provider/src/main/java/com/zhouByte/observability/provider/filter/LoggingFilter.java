package com.zhouByte.observability.provider.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Activate(group = {CommonConstants.PROVIDER}, order = -12000)
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        String interfaceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        Object[] args = invocation.getArguments();

        logger.info("[DUBBO-LOG] 接收到请求 | 服务={} | 方法={} | 参数类型数量={} | 参数值={}",
                interfaceName,
                methodName,
                invocation.getParameterTypes().length,
                args != null ? java.util.Arrays.toString(args) : "null");

        long startTime = System.currentTimeMillis();

        try {
            Result result = invoker.invoke(invocation);
            long costTime = System.currentTimeMillis() - startTime;

            logger.info("[DUBBO-LOG] 请求处理完成 | 服务={} | 方法={} | 耗时={}ms | 返回类型={}",
                    interfaceName,
                    methodName,
                    costTime,
                    result.getValue() != null ? result.getValue().getClass().getSimpleName() : "null");

            return result;
        } catch (RpcException e) {
            long costTime = System.currentTimeMillis() - startTime;

            logger.error("[DUBBO-LOG] 请求处理异常 | 服务={} | 方法={} | 耗时={}ms | 异常类型={} | 异常信息={}",
                    interfaceName,
                    methodName,
                    costTime,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);

            throw e;
        }
    }
}
