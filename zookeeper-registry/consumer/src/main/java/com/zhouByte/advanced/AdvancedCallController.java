package com.zhouByte.advanced;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 高级调用测试
 * 演示异步调用(CompletableFuture)和泛化调用(GenericService)
 * 
 * Dubbo 高级调用说明:
 * 
 * 1. 异步调用(@DubboReference + CompletableFuture):
 *    - Provider 返回 CompletableFuture<T> 类型
 *    - Consumer 通过 @DubboReference 引用异步服务
 *    - 可使用 .get() 阻塞等待或 .thenApply() 链式处理
 * 
 * 2. 泛化调用(ReferenceConfig + GenericService):
 *    - 无需依赖接口 JAR，通过接口全限定名动态调用
 *    - ReferenceConfig: 手动创建服务引用配置
 *      - setInterface(): 设置接口全限定名
 *      - setVersion(): 设置服务版本
 *      - setGroup(): 设置服务分组
 *      - setGeneric("true"): 开启泛化调用模式
 *      - setRegistry(): 设置注册中心地址
 *      - get(): 获取服务代理对象
 *      - destroy(): 释放资源
 *    - GenericService.$invoke(): 执行泛化调用
 *      - method: 方法名
 *      - parameterTypes: 参数类型全限定名数组
 *      - args: 实际参数值数组
 */
@RestController
@RequestMapping("/advanced")
public class AdvancedCallController {

    /**
     * 引用异步 UserService 服务
     * @DubboReference 配置:
     *   - interfaceClass = AsyncUserService.class: 指定异步服务接口
     *   - group = "advanced": 匹配 Provider 的分组
     *   - version = "1.0.0": 匹配 Provider 的版本号
     */
    @DubboReference(
            interfaceClass = AsyncUserService.class,
            group = "advanced",
            version = "1.0.0"
    )
    private AsyncUserService asyncUserService;

    /**
     * 引用同步 UserService 服务(用于对比异步性能)
     */
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
                - 提交请求耗时: %dms (非阻塞，立即返回 Future)
                - 等待结果耗时: %dms
                - 总耗时: %dms
                
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

        CompletableFuture<String> finalResult = step2.thenApply(finalData -> {
            logBuilder.append("Step 3: 最终结果 → ").append(finalData).append("\n");
            return finalData;
        });

        finalResult.get();

        long totalTime = System.currentTimeMillis() - startTime;

        logBuilder.append(String.format("\n⏱️ 链式总耗时: %dms\n", totalTime));
        logBuilder.append("\n💡 优势: 多个异步操作可并行执行，减少总体等待时间\n");

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
                
                🔵 同步调用: 耗时 %dms, 结果: %s
                🟢 异步调用: 耗时 %dms, 结果: %s
                
                📊 分析:
                - 单次调用性能相近
                - 异步优势体现在并发场景（多服务并行调用）
                - 高并发场景下，异步吞吐量更高
                """.formatted(syncCost, syncResult.replace("\n", " | "),
                             asyncCost, asyncResult.replace("\n", " | "));
    }

    /**
     * 泛化调用测试 - 无需依赖接口 JAR 即可调用远程服务
     * ReferenceConfig 说明:
     *   - 手动创建服务引用，适用于动态调用场景
     *   - 泛化调用模式下，返回值为 Map 类型(复杂对象会被转换)
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/generic/{username}/{password}")
    public String testGenericCall(@PathVariable String username, @PathVariable String password) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface("com.zhouByte.api.UserService");
        referenceConfig.setVersion("1.0.0");
        referenceConfig.setGroup("generic");
        referenceConfig.setGeneric("true");
        referenceConfig.setRegistry(new RegistryConfig("nacos://127.0.0.1:8848"));

        GenericService genericService = referenceConfig.get();

        try {
            Object result = genericService.$invoke(
                    "userLogin",
                    new String[]{"java.lang.String", "java.lang.String"},
                    new Object[]{username, password}
            );

            Map<String, Object> resultMap = (Map<String, Object>) result;

            return """
                    [泛化调用演示]
                    
                    特点: 无需依赖接口 JAR，动态指定接口名、方法名、参数类型
                    适用: API网关、测试平台、Mock平台
                    
                    调用信息:
                    - Interface: com.zhouByte.api.UserService
                    - Method: userLogin
                    - Parameters: [String, String]
                    
                    返回结果 (Map):
                    """ + formatMap(resultMap);

        } finally {
            referenceConfig.destroy();
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
