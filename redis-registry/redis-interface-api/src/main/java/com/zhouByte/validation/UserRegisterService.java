package com.zhouByte.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Email;

/**
 * 用户注册服务接口 - 用于演示参数验证功能
 * 
 * Dubbo 参数验证说明:
 *   - 使用 JSR303/Hibernate Validator 进行参数验证
 *   - Provider 端通过 @DubboService(validation = "jvalidation") 启用验证
 *   - Consumer 端通过 @DubboReference(validation = "true") 启用验证
 *   - 验证失败时抛出 ValidationException 异常
 * 
 * 常用验证注解:
 *   - @NotNull: 不能为 null
 *   - @NotBlank: 不能为 null 且去除空格后长度必须大于 0
 *   - @Size(min, max): 字符串长度或集合大小范围
 *   - @Min(value): 数值最小值
 *   - @Max(value): 数值最大值
 *   - @Email: 邮箱格式验证
 *   - @Pattern(regexp): 正则表达式验证
 * 
 * 验证位置:
 *   - 方法参数: 直接标注在参数上
 *   - 对象属性: 标注在 DTO 对象的字段上
 *   - 返回值: 标注在方法上验证返回值
 * 
 * 服务方法:
 *   - registerUser: 用户注册
 *     参数:
 *       - username: 用户名，@NotBlank + @Size(3-20)
 *       - password: 密码，@NotBlank + @Size(6-32)
 *       - email: 邮箱，@Email
 *       - age: 年龄，@Min(1)
 *     返回值: 注册结果
 */
public interface UserRegisterService {

    /**
     * 用户注册 - 演示方法参数验证
     * @param username 用户名，不能为空，长度 3-20 字符
     * @param password 密码，不能为空，长度 6-32 字符
     * @param email 邮箱，必须符合邮箱格式
     * @param age 年龄，必须大于等于 1
     * @return 注册结果
     */
    String registerUser(
            @NotBlank(message = "用户名不能为空")
            @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 字符之间")
            String username,

            @NotBlank(message = "密码不能为空")
            @Size(min = 6, max = 32, message = "密码长度必须在 6-32 字符之间")
            String password,

            @Email(message = "邮箱格式不正确")
            String email,

            @NotNull(message = "年龄不能为空")
            @Min(value = 1, message = "年龄必须大于等于 1")
            Integer age
    );

    /**
     * 更新用户信息 - 演示对象参数验证
     * @param user 用户信息对象，字段上标注了验证规则
     * @return 更新结果
     */
    String updateUserProfile(@NotNull(message = "用户信息不能为空") UserProfile user);
}
