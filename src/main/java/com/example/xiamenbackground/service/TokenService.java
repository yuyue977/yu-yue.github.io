package com.example.xiamenbackground.service;

import com.example.xiamenbackground.entity.User;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 服务 - 简单的内存 Token 管理
 */
@Service
public class TokenService {

    private final ConcurrentHashMap<String, User> tokenStore = new ConcurrentHashMap<>();

    /**
     * 为用户生成新 Token
     */
    public String generateToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user);
        return token;
    }

    /**
     * 验证 Token，返回对应用户
     */
    public User verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return tokenStore.get(token);
    }

    /**
     * 移除 Token（登出时使用）
     */
    public void removeToken(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }
}
