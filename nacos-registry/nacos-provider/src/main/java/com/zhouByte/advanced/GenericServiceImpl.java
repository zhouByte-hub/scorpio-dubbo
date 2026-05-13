package com.zhouByte.advanced;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.HashMap;
import java.util.Map;

/**
 * 泛化服务实现 - Consumer 无需依赖接口 JAR 即可调用
 * 通过 $invoke 方法动态分发请求
 * 
 * Dubbo 泛化调用说明:
 * @DubboService 配置:
 *   - interfaceClass = GenericService.class: 必须指定为 GenericService 接口
 *     GenericService 是 Dubbo 提供的泛化服务接口，允许动态调用任意服务
 *   - group = "generic": 服务分组，用于隔离泛化服务
 *   - version = "1.0.0": 服务版本号
 *   - parameters = {"interface", "com.zhouByte.api.UserService"}: 
 *     自定义参数，声明该泛化服务实际代理的目标接口全限定名
 * 
 * GenericService 接口:
 *   - $invoke(String method, String[] parameterTypes, Object[] args): 
 *     泛化调用入口方法
 *     参数说明:
 *       - method: 要调用的目标方法名，如 "userLogin"
 *       - parameterTypes: 方法参数类型的全限定名数组，如 ["java.lang.String", "java.lang.String"]
 *       - args: 实际的方法参数值数组
 *     返回值: 目标方法的返回结果
 * 
 * GenericException:
 *   - Dubbo 泛化调用专用异常，用于在泛化调用中抛出业务异常
 */
@DubboService(
        interfaceClass = GenericService.class,
        group = "generic",
        version = "1.0.0",
        parameters = {
                "interface", "com.zhouByte.api.UserService"
        }
)
public class GenericServiceImpl implements GenericService {

    @Override
    public Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
        if (method.equals("userLogin")) {
            return handleUserLogin(args);
        }
        throw new GenericException(new UnsupportedOperationException("不支持的方法: " + method));
    }

    private Object handleUserLogin(Object[] args) {
        if (args != null && args.length >= 2) {
            String username = (String) args[0];
            String password = (String) args[1];

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "泛化调用成功");
            result.put("data", "[GENERIC] " + username + " 通过泛化方式登录成功");
            result.put("username", username);
            result.put("loginTime", System.currentTimeMillis());
            result.put("callType", "Generic Invocation");

            return result;
        }
        throw new IllegalArgumentException("参数错误: 需要 username 和 password");
    }
}
