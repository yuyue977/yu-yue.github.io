package com.example.xiamenbackground.controller;

import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.repository.UserRepository;
import com.example.xiamenbackground.service.TokenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证 API 控制器 - 提供登录/验证/登出接口
 * 供 Unity WebView 登录页 AJAX 调用
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录接口 - 验证用户名密码，返回 Token
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username,
                                     @RequestParam String password,
                                     HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            result.put("success", false);
            result.put("message", "账号或密码错误");
            return result;
        }
        String token = tokenService.generateToken(user);
        session.setAttribute("loginUser", user);

        result.put("success", true);
        result.put("token", token);
        result.put("role", user.getRole());
        result.put("name", user.getName());
        return result;
    }

    /**
     * 验证 Token 接口
     */
    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        User user = tokenService.verifyToken(token);
        if (user == null) {
            result.put("valid", false);
            return result;
        }
        result.put("valid", true);
        result.put("role", user.getRole());
        result.put("name", user.getName());
        return result;
    }

    /**
     * 登出接口 - 移除 Token
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestParam(required = false) String token,
                                      HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (token != null) {
            tokenService.removeToken(token);
        }
        session.invalidate();
        result.put("success", true);
        return result;
    }
}
