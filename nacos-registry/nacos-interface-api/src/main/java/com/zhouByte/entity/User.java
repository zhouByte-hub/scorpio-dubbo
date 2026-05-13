package com.zhouByte.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;
    private String password;

}
