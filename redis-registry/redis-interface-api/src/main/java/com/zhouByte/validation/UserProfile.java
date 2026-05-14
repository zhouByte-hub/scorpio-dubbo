package com.zhouByte.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

/**
 * 用户信息 DTO - 用于演示对象参数验证
 * 
 * Dubbo 对象验证说明:
 *   - 验证注解标注在 DTO 对象的字段上
 *   - Provider 端接收到对象后自动验证所有字段
 *   - 任一字段验证失败即抛出 ValidationException
 * 
 * 字段验证规则:
 *   - username: @NotBlank + @Size(3-20)
 *   - email: @Email
 *   - phone: @Size(11) - 手机号固定 11 位
 *   - age: @Min(1)
 */
public class UserProfile {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 字符之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(min = 11, max = 11, message = "手机号必须为 11 位")
    private String phone;

    @Min(value = 1, message = "年龄必须大于等于 1")
    private Integer age;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", age=" + age +
                '}';
    }
}
