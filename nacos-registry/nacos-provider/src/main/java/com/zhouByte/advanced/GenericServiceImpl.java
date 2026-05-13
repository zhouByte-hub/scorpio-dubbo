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
 */
@DubboService(
        interfaceClass = GenericService.class,
        group = "generic",
        version = "1.0.0",
        generic = "true",
        parameters = {
                "interface", "com.zhouByte.api.UserService",
                "generic", "true"
        }
)
public class GenericServiceImpl implements GenericService {

    @Override
    public Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
        switch (method) {
            case "userLogin":
                return handleUserLogin(args);
            default:
                throw new GenericException(new UnsupportedOperationException("不支持的方法: " + method));
        }
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
