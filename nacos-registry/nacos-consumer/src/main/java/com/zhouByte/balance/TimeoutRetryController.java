package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/timeout")
public class TimeoutRetryController {

    @DubboReference(
            interfaceClass = UserService.class,
            timeout = 3000,
            retries = 2
    )
    private UserService normalTimeoutService;

    @DubboReference(
            interfaceClass = UserService.class,
            timeout = 1000,
            retries = 0
    )
    private UserService shortTimeoutNoRetryService;

    @GetMapping("/normal/{username}/{password}")
    public String testNormalTimeout(@PathVariable String username, @PathVariable String password) {
        long start = System.currentTimeMillis();
        try {
            String result = normalTimeoutService.userLogin(username, password);
            long cost = System.currentTimeMillis() - start;
            return result + "\n耗时: " + cost + "ms\n配置: timeout=3000ms, retries=2";
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            return "[TIMEOUT] 调用超时: " + e.getMessage() + "\n耗时: " + cost + "ms";
        }
    }

    @GetMapping("/strict/{username}/{password}")
    public String testStrictTimeout(@PathVariable String username, @PathVariable String password) {
        long start = System.currentTimeMillis();
        try {
            String result = shortTimeoutNoRetryService.userLogin(username, password);
            long cost = System.currentTimeMillis() - start;
            return result + "\n耗时: " + cost + "ms\n配置: timeout=1000ms, retries=0(不重试)";
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            return "[TIMEOUT] 调用超时: " + e.getMessage() + "\n耗时: " + cost + "ms\n严格模式：超时立即报错，不重试";
        }
    }
}
