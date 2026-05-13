package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 超时与重试机制测试
 * 对比 宽松超时(3s+重试2次) vs 严格超时(1s+不重试) 两种策略
 * 
 * Dubbo 超时与重试说明:
 * @DubboReference 配置:
 *   - timeout: 调用超时时间(毫秒)
 *     - 超过此时间未收到响应则抛出 RpcException
 *     - 默认值: 1000ms
 *     - 建议: 读操作可设置大些，写操作应设小些
 *   - retries: 失败重试次数(不含首次调用)
 *     - 0 表示不重试
 *     - 默认值: 2
 *     - 注意: 仅对幂等操作安全，写操作建议设为 0
 * 
 * 超时与重试的关系:
 *   - 总最大耗时 = timeout × (1 + retries)
 *   - 例如: timeout=3000, retries=2 → 最大耗时 9000ms
 */
@RestController
@RequestMapping("/timeout")
public class TimeoutRetryController {

    /**
     * 宽松超时配置: 3秒超时，允许重试2次，最大耗时9秒
     * @DubboReference 配置:
     *   - timeout = 3000: 单次调用超时 3 秒
     *   - retries = 2: 失败后最多重试 2 次
     */
    @DubboReference(
            interfaceClass = UserService.class,
            timeout = 3000,
            retries = 2
    )
    private UserService normalTimeoutService;

    /**
     * 严格超时配置: 1秒超时，不重试，最大耗时1秒
     * @DubboReference 配置:
     *   - timeout = 1000: 单次调用超时 1 秒
     *   - retries = 0: 失败后不重试
     */
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
