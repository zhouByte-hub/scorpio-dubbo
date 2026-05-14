package com.zhouByte.validation;

import com.zhouByte.validation.UserProfile;
import com.zhouByte.validation.UserRegisterService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

/**
 * 参数验证测试控制器
 * 演示 Dubbo 服务参数验证功能(JSR303/Hibernate Validator)
 * 
 * Dubbo 参数验证说明:
 * @DubboReference 配置:
 *   - validation = "true": 启用 Consumer 端参数验证
 *     验证时机:
 *       - Consumer 发送请求前验证参数
 *       - Provider 接收请求后验证参数(通过 validation = "jvalidation")
 *     验证失败:
 *       - 抛出 ValidationException 异常
 *       - 异常信息包含具体验证失败的字段和错误消息
 * 
 * 验证注解说明:
 *   - @NotNull: 不能为 null
 *   - @NotBlank: 不能为 null 且去除空格后长度 > 0
 *   - @Size(min, max): 字符串长度或集合大小范围
 *   - @Min(value): 数值最小值
 *   - @Email: 邮箱格式验证
 * 
 * 验证位置:
 *   - 方法参数: 直接标注在接口方法参数上
 *   - 对象属性: 标注在 DTO 对象的字段上
 *   - 返回值: 标注在方法上验证返回值
 * 
 * 最佳实践:
 *   - Provider 端必须启用验证(validation = "jvalidation")
 *   - Consumer 端可选启用验证(validation = "true")
 *   - 验证注解标注在接口上，实现类无需重复标注
 *   - 使用 message 属性提供友好的错误提示
 */
@RestController
@RequestMapping("/validation")
public class ValidationController {

    /**
     * 引用用户注册服务 - 启用参数验证
     * @DubboReference 配置:
     *   - interfaceClass = UserRegisterService.class: 指定服务接口
     *   - group = "validation-demo": 匹配服务分组
     *   - version = "1.0.0": 匹配服务版本号
     *   - validation = "true": 启用 Consumer 端参数验证
     */
    @DubboReference(
            interfaceClass = UserRegisterService.class,
            group = "validation-demo",
            version = "1.0.0",
            validation = "true"
    )
    private UserRegisterService userRegisterService;

    /**
     * 测试用户注册 - 验证通过
     */
    @GetMapping("/register/valid")
    public String testValidRegistration() {
        return """
                [参数验证 - 验证通过]
                所有参数符合验证规则
                
                """ + userRegisterService.registerUser(
                        "zhangsan",
                        "password123",
                        "zhangsan@example.com",
                        25
                );
    }

    /**
     * 测试用户注册 - 用户名为空(验证失败)
     */
    @GetMapping("/register/invalid-username")
    public String testInvalidUsername() {
        try {
            return userRegisterService.registerUser(
                    "",
                    "password123",
                    "zhangsan@example.com",
                    25
            );
        } catch (Exception e) {
            return """
                    [参数验证 - 验证失败]
                    错误类型: %s
                    错误信息: %s
                    
                    验证规则: 用户名不能为空，长度 3-20 字符
                    """.formatted(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * 测试用户注册 - 密码太短(验证失败)
     */
    @GetMapping("/register/invalid-password")
    public String testInvalidPassword() {
        try {
            return userRegisterService.registerUser(
                    "zhangsan",
                    "123",
                    "zhangsan@example.com",
                    25
            );
        } catch (Exception e) {
            return """
                    [参数验证 - 验证失败]
                    错误类型: %s
                    错误信息: %s
                    
                    验证规则: 密码长度必须在 6-32 字符之间
                    """.formatted(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * 测试用户注册 - 邮箱格式错误(验证失败)
     */
    @GetMapping("/register/invalid-email")
    public String testInvalidEmail() {
        try {
            return userRegisterService.registerUser(
                    "zhangsan",
                    "password123",
                    "invalid-email",
                    25
            );
        } catch (Exception e) {
            return """
                    [参数验证 - 验证失败]
                    错误类型: %s
                    错误信息: %s
                    
                    验证规则: 邮箱格式不正确
                    """.formatted(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * 测试用户注册 - 年龄小于 1(验证失败)
     */
    @GetMapping("/register/invalid-age")
    public String testInvalidAge() {
        try {
            return userRegisterService.registerUser(
                    "zhangsan",
                    "password123",
                    "zhangsan@example.com",
                    0
            );
        } catch (Exception e) {
            return """
                    [参数验证 - 验证失败]
                    错误类型: %s
                    错误信息: %s
                    
                    验证规则: 年龄必须大于等于 1
                    """.formatted(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * 测试更新用户信息 - 对象参数验证
     */
    @PostMapping("/update")
    public String testUpdateUserProfile() {
        UserProfile validProfile = new UserProfile();
        validProfile.setUsername("lisi");
        validProfile.setEmail("lisi@example.com");
        validProfile.setPhone("13800138000");
        validProfile.setAge(30);

        return """
                [参数验证 - 对象参数验证通过]
                
                """ + userRegisterService.updateUserProfile(validProfile);
    }

    /**
     * 测试更新用户信息 - 对象参数验证失败
     */
    @PostMapping("/update/invalid")
    public String testUpdateUserProfileInvalid() {
        UserProfile invalidProfile = new UserProfile();
        invalidProfile.setUsername("ab");
        invalidProfile.setEmail("invalid");
        invalidProfile.setPhone("123");
        invalidProfile.setAge(0);

        try {
            return userRegisterService.updateUserProfile(invalidProfile);
        } catch (Exception e) {
            return """
                    [参数验证 - 对象参数验证失败]
                    错误类型: %s
                    错误信息: %s
                    """.formatted(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * 参数验证说明文档
     */
    @GetMapping("/doc")
    public String getDoc() {
        return """
                [Dubbo 参数验证说明]
                
                验证注解:
                  - @NotNull    - 不能为 null
                  - @NotBlank   - 不能为空且长度 > 0
                  - @Size       - 长度/大小范围
                  - @Min/@Max   - 数值范围
                  - @Email      - 邮箱格式
                  - @Pattern    - 正则表达式
                
                验证配置:
                  - Provider: validation = "jvalidation"
                  - Consumer: validation = "true"
                
                测试接口:
                  - GET  /validation/register/valid          - 验证通过
                  - GET  /validation/register/invalid-username - 用户名验证失败
                  - GET  /validation/register/invalid-password - 密码验证失败
                  - GET  /validation/register/invalid-email    - 邮箱验证失败
                  - GET  /validation/register/invalid-age      - 年龄验证失败
                  - POST /validation/update                   - 对象验证通过
                  - POST /validation/update/invalid           - 对象验证失败
                """;
    }
}
