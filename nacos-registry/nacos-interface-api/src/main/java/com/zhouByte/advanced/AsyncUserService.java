package com.zhouByte.advanced;

import java.util.concurrent.CompletableFuture;

/**
 * 异步用户服务接口 - 支持 CompletableFuture 异步调用和同步调用
 */
public interface AsyncUserService {

    /**
     * 异步登录 - 返回 CompletableFuture，不阻塞调用线程
     */
    CompletableFuture<String> asyncLogin(String username, String password);

    /**
     * 同步登录 - 传统阻塞式调用，用于与异步方式对比
     */
    String syncLogin(String username, String password);
}
