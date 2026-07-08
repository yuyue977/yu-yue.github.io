package com.example.xiamenbackground.controller;

import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, Model model) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "账号或密码错误");
            return "login";
        }
        session.setAttribute("loginUser", user);
        
        // 根据角色重定向到不同页面
        String role = user.getRole();
        if ("管理员".equals(role)) {
            return "redirect:/users"; // 管理员进入用户管理
        } else if ("调度主管".equals(role)) {
            return "redirect:/equipment"; // 调度主管进入设备基础信息
        } else if ("调度员".equals(role)) {
            return "redirect:/production"; // 调度员进入设备生产信息
        }
        return "redirect:/users"; // 默认
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";
        
        // 根据角色重定向到不同页面
        String role = user.getRole();
        if ("管理员".equals(role)) {
            return "redirect:/users";
        } else if ("调度主管".equals(role)) {
            return "redirect:/equipment";
        } else if ("调度员".equals(role)) {
            return "redirect:/production";
        }
        return "redirect:/users";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
