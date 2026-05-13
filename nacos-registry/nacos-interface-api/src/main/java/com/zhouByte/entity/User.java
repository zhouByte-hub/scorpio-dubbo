package com.zhouByte.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类 - Dubbo RPC 传输数据对象
 * 
 * Dubbo 实体类说明:
 *   - 作为 Dubbo RPC 调用的参数或返回值传输
 *   - 必须实现 Serializable 接口(或使用 Lombok 的 @Data 等注解)
 *   - 所有字段类型必须是可序列化的
 * 
 * Lombok 注解说明:
 *   - @Data: 自动生成 getter/setter/toString/equals/hashCode 方法
 *   - @AllArgsConstructor: 自动生成全参数构造函数
 *   - @NoArgsConstructor: 自动生成无参数构造函数
 * 
 * 字段说明:
 *   - username: 用户名，String 类型
 *   - password: 密码，String 类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;

}
