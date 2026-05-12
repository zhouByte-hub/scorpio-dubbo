package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/balance")
public class LoadBalanceController {

    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-random",
            loadbalance = "random",
            cluster = "failover"
    )
    private UserService randomUserService;

    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-roundrobin",
            loadbalance = "roundrobin",
            cluster = "failover"
    )
    private UserService roundrobinUserService;

    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-leastactive",
            loadbalance = "leastactive",
            cluster = "failover"
    )
    private UserService leastActiveUserService;

    @DubboReference(
            interfaceClass = UserService.class,
            group = "balance-consistenthash",
            loadbalance = "consistenthash",
            cluster = "failover"
    )
    private UserService consistentHashUserService;

    @GetMapping("/random/{username}/{password}")
    public String testRandomBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(randomUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }

    @GetMapping("/roundrobin/{username}/{password}")
    public String testRoundRobinBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(roundrobinUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }

    @GetMapping("/leastactive/{username}/{password}")
    public String testLeastActiveBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(leastActiveUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }

    @GetMapping("/consistenthash/{username}/{password}")
    public String testConsistentHashBalance(@PathVariable String username, @PathVariable String password) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(consistentHashUserService.userLogin(username, password));
        }
        return String.join("\n", results);
    }
}
