package com.zhouByte.observability;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 结构化日志记录过滤器 - 提供 Dubbo 服务的标准化日志输出
 *
 * <h2>功能概述</h2>
 * 该过滤器实现了统一的服务调用日志记录规范，确保所有 Dubbo 请求都有
 * 结构化的日志输出，便于问题排查、审计追踪和运营分析。
 *
 * <h2>解决的问题</h2>
 * <ol>
 *   <li><b>日志格式不统一</b> - 不同开发者的日志风格各异，难以自动化解析</li>
 *   <li><b>关键信息缺失</b> - 部分请求缺少参数、耗时、返回值等重要字段</li>
 *   <li><b>异常信息不足</b> - 异常日志往往只有堆栈，缺少上下文信息</li>
 *   <li><b>性能数据分散</b> - 响应时间散落在各处，无法集中分析</li>
 * </ol>
 *
 * <h2>日志内容设计</h2>
 * <h3>请求开始日志</h3>
 * <pre>
 * [DUBBO-LOG] 接收到请求 |
 *   服务=com.zhouByte.api.UserService |  ← 接口全限定名
 *   方法=login |                           ← 方法名
 *   参数类型数量=2 |                       ← 参数个数
 *   参数值=[user1, pwd123]               ← 参数值（注意脱敏）
 * </pre>
 *
 * <h3>请求完成日志</h3>
 * <pre>
 * [DUBBO-LOG] 请求处理完成 |
 *   服务=com.zhouByte.api.UserService |
 *   方法=login |
 *   耗时=45ms |                          ← 性能指标
 *   返回类型=User                         ← 返回值类型
 * </pre>
 *
 * <h3>异常日志</h3>
 * <pre>
 * [DUBBO-LOG] 请求处理异常 |
 *   服务=com.zhouByte.api.UserService |
 *   方法=login |
 *   耗时=120ms |
 *   异常类型=RpcException |              ← 异常分类
 *   异常信息=Connection timeout |         ← 简短描述
 *   完整堆栈=...                          ← 详细堆栈（仅在 ERROR 级别）
 * </pre>
 *
 * <h2>最佳实践建议</h2>
 * <ul>
 *   <li><b>敏感数据脱敏</b> - 生产环境中应对密码、手机号等参数进行掩码处理</li>
 *   <li><b>日志级别控制</b> - INFO 记录正常流程，ERROR 仅记录异常</li>
 *   <li><b>采样率设置</b> - 高流量场景下可考虑抽样记录（如只记录 10%）</li>
 *   <li><b>异步输出</b> - 使用 AsyncAppender 避免阻塞业务线程</li>
 * </ul>
 *
 * <h2>与其他过滤器的协作</h2>
 * <pre>
 * 请求进入时的执行顺序：
 * 1. LoggingFilter (-12000)  ← 最先执行，记录请求详情
 * 2. TracingFilter (-11000)  ← 提取/生成 TraceId
 * 3. MetricsFilter (-10000)  ← 开始计时
 * 4. 业务逻辑执行
 * 5. MetricsFilter           ← 结束计时，更新指标
 * 6. TracingFilter           ← 记录耗时到 Span
 * 7. LoggingFilter           ← 最后执行，记录结果
 * </pre>
 *
 * <h2>日志分析场景</h2>
 * <table border="1">
 *   <tr><th>需求</th><th>查询语句</th><th>预期结果</th></tr>
 *   <tr><td>查找慢请求</td><td>耗时 &gt; 1000ms</td><td>性能瓶颈列表</td></tr>
 *   <tr><td>统计调用频率</td><td>按方法名 GROUP BY</td><td>热点方法排行</td></tr>
 *   <tr><td>异常趋势分析</td><td>按小时统计 ERROR 数量</td><td>故障时间线</td></tr>
 *   <tr><td>参数合法性检查</td><td>筛选空值/特殊字符</td><td>恶意请求检测</td></tr>
 * </table>
 *
 * <h2>性能考量</h2>
 * <ul>
 *   <li>日志 I/O 是主要开销（通常 1-10ms/条）</li>
 *   <li>建议使用 Logback/Log4j2 的异步 Appender</li>
 *   <li>避免在日志中打印大对象（如 List&lt;Object&gt; 可能很大）</li>
 *   <li>参数值使用 Arrays.toString() 会调用 toString()，注意性能</li>
 * </ul>
 *
 * @author zhouByte
 * @version 1.0.0
 * @see org.apache.dubbo.rpc.Filter
 * @see MetricsFilter
 * @see TracingFilter
 */
@Activate(group = {CommonConstants.PROVIDER}, order = -12000)
public class LoggingFilter implements Filter {

    /**
     * 日志记录器 - 专用于输出 Dubbo 调用日志
     *
     * <p><b>配置建议：</b>
     * <pre>
     * &lt;!-- logback.xml --&gt;
     * &lt;logger name="com.zhouByte.observability.LoggingFilter" level="INFO" additivity="false"&gt;
     *   &lt;appender-ref ref="DUBBO_LOG_FILE"/&gt;
     * &lt;/logger&gt;
     * </pre>
     *
     * <p>将 Dubbo 日志单独输出到文件，便于：
     * <ul>
     *   <li>独立设置保留策略（如保留 30 天）</li>
     *   <li>单独配置归档压缩</li>
     *   <li>授权不同团队访问</li>
     * </ul>
     */
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    /**
     * 过滤器核心方法 - 实现请求生命周期的日志记录
     *
     * <h3>日志记录时机</h3>
     * <ol>
     *   <li><b>请求到达时</b>（invoke 方法入口）
     *       <ul>
     *         <li>记录服务接口名、方法名</li>
     *         <li>记录参数类型和参数值</li>
     *         <li>此时还未执行业务逻辑，用于建立请求基线</li>
     *       </ul>
     *   </li>
     *   <li><b>请求完成时</b>（try 块结尾）
     *       <ul>
     *         <li>记录处理耗时（性能指标）</li>
     *         <li>记录返回值类型（帮助理解数据流）</li>
     *         <li>标记请求成功结束</li>
     *       </ul>
     *   </li>
     *   <li><b>请求异常时</b>（catch 块）
     *       <ul>
     *         <li>记录异常类型和消息（快速定位问题类别）</li>
     *         <li>记录完整异常堆栈（详细调试信息）</li>
     *         <li>仍然记录耗时（异常路径也可能很慢）</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <h3>关键字段说明</h3>
     * <ul>
     *   <li><b>interfaceName</b> - 接口的全限定类名，用于定位具体服务</li>
     *   <li><b>methodName</b> - 方法名，用于定位具体操作</li>
 *   <li><b>args</b> - 方法参数数组，用于重现调用场景</li>
     *   <li><b>costTime</b> - 耗时（毫秒），用于性能分析</li>
     *   <li><b>result.getValue()</b> - 返回值对象，用于验证正确性</li>
     * </ul>
     *
     * <h3>异常处理策略</h3>
     * <ul>
     *   <li>捕获所有 RpcException 及其子类</li>
     *   <li>记录异常详细信息后重新抛出（不吞掉异常）</li>
     *   <li>确保上层能感知到异常并进行相应处理</li>
     * </ul>
     *
     * @param invoker 服务调用器，封装了目标服务的调用信息
     * @param invocation 调用上下文，包含方法签名和参数
     * @return Result 调用结果，包含返回值或异常
     * @throws RpcException 当远程调用失败时抛出
     */
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
