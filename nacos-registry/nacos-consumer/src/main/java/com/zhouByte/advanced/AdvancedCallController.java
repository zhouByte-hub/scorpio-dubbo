package com.zhouByte.advanced;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/advanced")
public class AdvancedCallController {

    @DubboReference(
            interfaceClass = AsyncUserService.class,
            group = "advanced",
            version = "1.0.0"
    )
    private AsyncUserService asyncUserService;

    @DubboReference(
            interfaceClass = AsyncUserService.class,
            group = "advanced",
            version = "1.0.0"
    )
    private AsyncUserService syncUserServiceForComparison;

    @GetMapping("/async/{username}/{password}")
    public String testAsyncCall(@PathVariable String username, @PathVariable String password) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        CompletableFuture<String> future = asyncUserService.asyncLogin(username, password);

        long submitTime = System.currentTimeMillis() - startTime;

        String result = future.get();

        long totalTime = System.currentTimeMillis() - startTime;

        return """
                [异步调用演示]
                
                ⏱️ 时间统计:
                - 提交请求耗时: %dms (非阻塞，立即返回 Future 对象)
                - 等待结果耗时: %dms (阻塞等待实际结果)
                - 总耗时: %dms
                
                📝 调用特点:
                ✅ 不阻塞 Consumer 线程
                ✅ 返回 CompletableFuture 对象
                ✅ 可链式操作: thenApply / thenAccept / thenCompose
                ✅ 支持超时控制: get(timeout, unit)
                
                调用结果:
                """.formatted(submitTime, totalTime - submitTime, totalTime) 
                + result;
    }

    @GetMapping("/async-chain/{username}/{password}")
    public String testAsyncChainCall(@PathVariable String username, @PathVariable String password) throws Exception {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("[异步链式调用演示]\n\n");

        long startTime = System.currentTimeMillis();

        CompletableFuture<String> step1 = asyncUserService.asyncLogin(username, password)
                .thenApply(result -> {
                    logBuilder.append("Step 1: 登录完成 → ").append(result.substring(0, Math.min(result.length(), 50))).append("\n");
                    return "用户 " + username + " 已认证";
                });

        CompletableFuture<String> step2 = step1.thenCompose(authResult -> {
            logBuilder.append("Step 2: 开始查询权限...\n");
            return CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return authResult + " | 权限: [READ, WRITE]";
            });
        });

        CompletableFuture<String> finalResult = step2.thenAccept(finalData -> {
            logBuilder.append("Step 3: 最终结果 → ").append(finalData).append("\n");
        });

        finalResult.get();

        long totalTime = System.currentTimeMillis() - startTime;

        logBuilder.append(String.format("\n⏱️ 链式总耗时: %dms\n", totalTime));
        logBuilder.append("\n💡 优势说明:\n");
        logBuilder.append("- 多个异步操作可并行执行\n");
        logBuilder.append("- 减少总体等待时间\n");
        logBuilder.append("- 提升系统吞吐量\n");

        return logBuilder.toString();
    }

    @GetMapping("/sync-vs-async/{username}/{password}")
    public String compareSyncAndAsync(@PathVariable String username, @PathVariable String password) throws Exception {
        long syncStart = System.currentTimeMillis();
        String syncResult = syncUserServiceForComparison.syncLogin(username, password);
        long syncCost = System.currentTimeMillis() - syncStart;

        long asyncStart = System.currentTimeMillis();
        CompletableFuture<String> asyncFuture = asyncUserService.asyncLogin(username, password);
        String asyncResult = asyncFuture.get();
        long asyncCost = System.currentTimeMillis() - asyncStart;

        return """
                [同步 vs 异步对比]
                
                🔵 同步调用:
                耗时: %dms
                结果: %s
                
                🟢 异步调用:
                耗时: %dms
                结果: %s
                
                📊 分析:
                - 单次调用性能相近
                - 异步优势体现在并发场景
                - 异步不阻塞线程，可同时处理其他任务
                - 高并发场景下，异步吞吐量更高
                """.formatted(syncCost, syncResult.replace("\n", " | "), 
                             asyncCost, asyncResult.replace("\n", " | "));
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/generic/{username}/{password}")
    public String testGenericCall(@PathVariable String username, @PathVariable String password) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface("com.zhouByte.api.UserService");
        referenceConfig.setVersion("1.0.0");
        referenceConfig.setGroup("generic");
        referenceConfig.setGeneric("true");

        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService = cache.get(referenceConfig);

        try {
            Object result = genericService.$invoke(
                    "userLogin",
                    new String[]{"java.lang.String", "java.lang.String"},
                    new Object[]{username, password}
            );

            Map<String, Object> resultMap = (Map<String, Object>) result;

            return """
                    [泛化调用演示]
                    
                    ✨ 特点:
                    - 无需依赖 UserService 接口 JAR 包
                    - 动态指定接口名、方法名、参数类型
                    - 返回值类型为 Object（通常是 Map）
                    
                    🎯 适用场景:
                    - API 网关统一转发
                    - 测试平台动态调用
                    - 服务Mock平台
                    - 泛型HTTP转RPC网关
                    
                    📋 调用信息:
                    - Interface: com.zhouByte.api.UserService
                    - Method: userLogin
                    - Parameters: [String, String]
                    
                    📦 返回结果 (Map):
                    """ + formatMap(resultMap);

        } finally {
            cache.destroy(referenceConfig);
        }
    }

    private String formatMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
