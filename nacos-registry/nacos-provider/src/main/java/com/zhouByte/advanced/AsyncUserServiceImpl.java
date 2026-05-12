package com.zhouByte.advanced;

import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
