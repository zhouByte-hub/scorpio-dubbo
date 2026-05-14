package com.zhouByte.validation;

import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户注册服务实现 - 演示参数验证功能
 * 
 * Dubbo 参数验证说明:
 * @DubboService 配置:
 *   - interfaceClass = UserRegisterService.class: 指定暴露的服务接口
 *   - group = "validation-demo": 服务分组，标识此服务启用了参数验证
 *   - version = "1.0.0": 服务版本号
 *   - validation = "jvalidation": 启用 JSR303 参数验证
 *     验证器类型:
 *       - jvalidation: 使用 JSR303/Hibernate Validator 进行验证
 *       - 验证失败时抛出 ValidationException 异常
 * 
 * 验证流程:
 *   1. Consumer 发起调用请求
 *   2. Dubbo 框架在 Provider 端自动验证参数
 *   3. 验证通过则执行业务逻辑
 *   4. 验证失败则直接返回错误，不执行业务逻辑
 * 
 * 注意:
 *   - 验证注解标注在接口方法参数上
 *   - Provider 实现类无需额外配置验证逻辑
 *   - 验证由 Dubbo 框架自动完成
 */
@DubboService(
        interfaceClass = UserRegisterService.class,
        group = "validation-demo",
        version = "1.0.0",
        validation = "jvalidation"
)
public class UserRegisterServiceImpl implements UserRegisterService {

    private static final Logger logger = LoggerFactory.getLogger(UserRegisterServiceImpl.class);

    @Override
    public String registerUser(String username, String password, String email, Integer age) {
        logger.info("用户注册成功: username={}, email={}, age={}", username, email, age);
        return "[参数验证通过] 用户注册成功\n"
                + "- username: " + username + "\n"
                + "- email: " + email + "\n"
                + "- age: " + age + "\n"
                + "- userId: USER-" + System.currentTimeMillis();
    }

    @Override
    public String updateUserProfile(UserProfile user) {
        logger.info("用户信息更新成功: {}", user);
        return "[参数验证通过] 用户信息更新成功\n"
                + "- " + user.toString();
    }
}
