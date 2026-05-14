package com.zhouByte.api;

/**
 * 用户服务接口 - Dubbo RPC 服务契约
 * 
 * Dubbo 接口说明:
 *   - 此接口定义了 Provider 和 Consumer 之间的服务契约
 *   - Provider 实现此接口并通过 @DubboService 暴露为远程服务
 *   - Consumer 通过 @DubboReference 引用此接口进行远程调用
 *   - 接口方法参数和返回值必须是可序列化的类型
 * 
 * 服务方法:
 *   - userLogin: 用户登录方法
 *     参数:
 *       - username: 用户名，String 类型
 *       - password: 密码，String 类型
 *     返回值: 登录结果信息，String 类型
 */
public interface UserService {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果信息
     */
    String userLogin(String username, String password);

}
