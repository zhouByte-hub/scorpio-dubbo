package com.zhouByte.advanced;

import java.util.concurrent.CompletableFuture;

public interface AsyncUserService {
    CompletableFuture<String> asyncLogin(String username, String password);
    String syncLogin(String username, String password);
}
