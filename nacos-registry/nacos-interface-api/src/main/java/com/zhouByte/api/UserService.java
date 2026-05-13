package com.zhouByte.api;

/**
 * 用户服务接口 - Dubbo RPC 服务契约
 */
public interface UserService {

    /**
     * 用户登录
     */
    String userLogin(String username, String password);

}
