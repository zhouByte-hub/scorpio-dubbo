package com.zhouByte.advanced;

import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步用户服务实现 - 使用独立线程池避免阻塞 Dubbo IO 线程
 * 
 * Dubbo 异步调用说明:
 * @DubboService 配置:
 *   - interfaceClass = AsyncUserService.class: 显式指定暴露的接口类型
 *   - group = "advanced": 服务分组为 advanced，用于服务隔离
 *   - version = "1.0.0": 服务版本号，Consumer 需匹配相同版本才能调用
 * 
 * CompletableFuture 异步返回:
 *   - Dubbo 支持返回 CompletableFuture<T> 类型
 *   - Provider 端异步处理，不阻塞 Dubbo 线程池
 *   - Consumer 端可通过 .get() 阻塞等待或 .thenApply() 链式处理
 */
@DubboService(
        interfaceClass = AsyncUserService.class,
        group = "advanced",
        version = "1.0.0"
)
public class AsyncUserServiceImpl implements AsyncUserService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncUserServiceImpl.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public CompletableFuture<String> asyncLogin(String username, String password) {
        logger.info("[异步服务] 接收到异步登录请求 | 用户名={}", username);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep((long) (Math.random() * 200 + 100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            logger.info("[异步服务] 异步处理完成 | 用户名={}", username);

            return "[ASYNC] " + username + " 异步登录成功\n"
                    + "- 使用线程池: FixedThreadPool(10)\n"
                    + "- 模拟耗时: 100-300ms\n"
                    + "- 返回类型: CompletableFuture<String>";
        }, executorService);
    }

    @Override
    public String syncLogin(String username, String password) {
        logger.info("[同步服务] 接收到同步登录请求 | 用户名={}", username);

        try {
            Thread.sleep((long) (Math.random() * 200 + 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("[同步服务] 同步处理完成 | 用户名={}", username);

        return "[SYNC] " + username + " 同步登录成功\n"
                + "- 阻塞当前线程\n"
                + "- 模拟耗时: 100-300ms";
    }
}
