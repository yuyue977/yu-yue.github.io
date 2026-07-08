package com.example.xiamenbackground.controller;

import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.repository.UserRepository;
import com.example.xiamenbackground.util.PermissionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canManageUsers(session)) return "redirect:/equipment";
        List<User> allUsers = userRepository.findAll();
        List<User> adminUsers = new ArrayList<>();
        List<User> supervisorUsers = new ArrayList<>();
        List<User> dispatcherUsers = new ArrayList<>();
        for (User u : allUsers) {
            if ("管理员".equals(u.getRole())) adminUsers.add(u);
            else if ("调度主管".equals(u.getRole())) supervisorUsers.add(u);
            else if ("调度员".equals(u.getRole())) dispatcherUsers.add(u);
        }
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("supervisorUsers", supervisorUsers);
        model.addAttribute("dispatcherUsers", dispatcherUsers);
        model.addAttribute("users", allUsers);
        model.addAttribute("user", loginUser);

        // 读取并清除删除错误消息
        String deleteError = (String) session.getAttribute("deleteError");
        if (deleteError != null) {
            model.addAttribute("deleteError", deleteError);
            session.removeAttribute("deleteError");
        }

        return "user/list";
    }

    @PostMapping("/add")
    public String add(@RequestParam String name, @RequestParam String role,
                      @RequestParam String telephone, @RequestParam String username,
                      @RequestParam String password,
                      HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canManageUsers(session)) return "redirect:/equipment";

        User user = new User();
        user.setName(name);
        user.setRole(role);
        user.setTelephone(telephone);
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);
        return "redirect:/users";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @RequestParam String name,
                         @RequestParam String role, @RequestParam String telephone,
                         @RequestParam(required = false) String username,
                         @RequestParam(required = false) String password,
                         HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canManageUsers(session)) return "redirect:/equipment";

        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setName(name);
            user.setRole(role);
            user.setTelephone(telephone);
            if (username != null && !username.isEmpty()) {
                user.setUsername(username);
            }
            if (password != null && !password.isEmpty()) {
                user.setPassword(password);
            }
            userRepository.save(user);
        }
        return "redirect:/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canManageUsers(session)) return "redirect:/equipment";

        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser == null) return "redirect:/users";

        // admin 账号为系统内置账号，永远不允许删除
        if ("admin".equals(targetUser.getUsername())) {
            session.setAttribute("deleteError", "admin 为系统内置账号，不允许删除，请确保始终有一个可登录的管理员账号");
            return "redirect:/users";
        }

        userRepository.deleteById(id);
        return "redirect:/users";
    }

    // ---- AJAX JSON 接口：供调度主管 / 调度员 Tab 无刷新操作使用 ----

    @PostMapping("/api/add")
    @ResponseBody
    public Map<String, Object> apiAdd(@RequestParam String name, @RequestParam String role,
                                      @RequestParam String telephone, @RequestParam String username,
                                      @RequestParam String password, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) { result.put("success", false); result.put("message", "未登录"); return result; }
        if (!PermissionUtil.canManageUsers(session)) { result.put("success", false); result.put("message", "无权限"); return result; }

        User user = new User();
        user.setName(name);
        user.setRole(role);
        user.setTelephone(telephone);
        user.setUsername(username);
        user.setPassword(password);
        User saved = userRepository.save(user);

        Map<String, Object> u = new HashMap<>();
        u.put("id", saved.getId());
        u.put("name", saved.getName());
        u.put("role", saved.getRole());
        u.put("telephone", saved.getTelephone());
        u.put("username", saved.getUsername());

        result.put("success", true);
        result.put("user", u);
        return result;
    }

    @PostMapping("/api/delete/{id}")
    @ResponseBody
    public Map<String, Object> apiDelete(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) { result.put("success", false); result.put("message", "未登录"); return result; }
        if (!PermissionUtil.canManageUsers(session)) { result.put("success", false); result.put("message", "无权限"); return result; }

        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser == null) { result.put("success", false); result.put("message", "用户不存在"); return result; }

        // admin 账号为系统内置账号，永远不允许删除
        if ("admin".equals(targetUser.getUsername())) {
            result.put("success", false);
            result.put("message", "admin 为系统内置账号，不允许删除");
            return result;
        }

        userRepository.deleteById(id);
        result.put("success", true);
        return result;
    }
}
