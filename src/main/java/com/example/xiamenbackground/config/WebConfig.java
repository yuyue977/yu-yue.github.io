package com.example.xiamenbackground.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册权限拦截器，排除登录页面和静态资源
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/api/auth/login", "/css/**", "/js/**", "/images/**");
    }
}
