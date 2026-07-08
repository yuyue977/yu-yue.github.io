package com.example.xiamenbackground.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.xiamenbackground.entity.User;

/**
 * 权限拦截器 - 根据用户角色控制访问权限
 * 
 * 管理员：所有权限
 * 调度主管：设备基础信息编辑 + 设备生产信息查看/导出（不能管理用户）
 * 调度员：仅查看设备生产信息（不能编辑、不能导出）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        // 未登录用户重定向到登录页
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }
        
        String role = user.getRole();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // WebSocket 和 API 端点允许所有已登录角色访问
        if (uri.startsWith("/ws/") || uri.startsWith("/api/")) {
            return true;
        }
        
        // 退出登录允许所有角色
        if (uri.startsWith("/logout")) {
            return true;
        }
        
        // 管理员可以访问所有页面和操作
        if ("管理员".equals(role)) {
            return true;
        }
        
        // 调度主管的权限
        if ("调度主管".equals(role)) {
            // 禁止访问用户管理
            if (uri.startsWith("/users")) {
                response.sendRedirect("/equipment");
                return false;
            }
            // 允许访问设备基础信息和生产信息（包括编辑和导出）
            if (uri.startsWith("/equipment") || 
                uri.startsWith("/production") || 
                uri.startsWith("/silos") || 
                uri.startsWith("/hoppers") || 
                uri.startsWith("/loaders")) {
                return true;
            }
            return true;
        }
        
        // 调度员的权限限制
        if ("调度员".equals(role)) {
            // 只允许 GET 访问设备生产信息页面（查看）
            if (uri.startsWith("/production") && "GET".equalsIgnoreCase(method)) {
                return true;
            }
            // 禁止访问用户管理和设备基础信息
            if (uri.startsWith("/users") || uri.startsWith("/equipment") ||
                uri.startsWith("/silos") || uri.startsWith("/hoppers") || 
                uri.startsWith("/loaders")) {
                response.sendRedirect("/production");
                return false;
            }
            // 禁止调度员的 POST 请求（导出、编辑等）
            if ("POST".equalsIgnoreCase(method)) {
                response.sendRedirect("/production");
                return false;
            }
            return true;
        }
        
        // 其他角色默认允许访问
        return true;
    }
}
