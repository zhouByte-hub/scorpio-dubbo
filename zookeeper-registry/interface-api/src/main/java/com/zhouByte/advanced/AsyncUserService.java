package com.zhouByte.advanced;

import java.util.concurrent.CompletableFuture;

/**
 * 异步用户服务接口 - 支持 CompletableFuture 异步调用和同步调用
 * 
 * Dubbo 异步调用说明:
 *   - 此接口定义了支持异步调用的服务契约
 *   - Provider 返回 CompletableFuture<T> 实现异步处理
 *   - Consumer 可通过 .get() 阻塞等待或 .thenApply() 链式处理
 * 
 * CompletableFuture 说明:
 *   - Java 8 引入的异步编程工具类
 *   - 支持链式调用: thenApply, thenCompose, thenAccept 等
 *   - 支持异常处理: exceptionally, handle 等
 *   - 支持组合: allOf, anyOf 等
 * 
 * 服务方法:
 *   - asyncLogin: 异步登录方法
 *     参数:
 *       - username: 用户名，String 类型
 *       - password: 密码，String 类型
 *     返回值: CompletableFuture<String>，异步返回登录结果
 *   - syncLogin: 同步登录方法(用于对比)
 *     参数:
 *       - username: 用户名，String 类型
 *       - password: 密码，String 类型
 *     返回值: String，同步返回登录结果
 */
public interface AsyncUserService {

    /**
     * 异步登录 - 返回 CompletableFuture，不阻塞调用线程
     * @param username 用户名
     * @param password 密码
     * @return CompletableFuture<String> 异步登录结果
     */
    CompletableFuture<String> asyncLogin(String username, String password);

    /**
     * 同步登录 - 传统阻塞式调用，用于与异步方式对比
     * @param username 用户名
     * @param password 密码
     * @return String 同步登录结果
     */
    String syncLogin(String username, String password);
}
