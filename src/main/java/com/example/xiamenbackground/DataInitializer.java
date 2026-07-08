package com.example.xiamenbackground;

import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User existingAdmin = userRepository.findByUsername("admin").orElse(null);
        if (existingAdmin != null) {
            if (!"管理员".equals(existingAdmin.getRole())) {
                existingAdmin.setRole("管理员");
                userRepository.save(existingAdmin);
                System.out.println(">>> 管理员角色已更新为: 管理员");
            }
        } else {
            User admin = new User();
            admin.setName("管理员");
            admin.setRole("管理员");
            admin.setTelephone("00000000000");
            admin.setUsername("admin");
            admin.setPassword("admin123");
            userRepository.save(admin);
            System.out.println(">>> 默认管理员账号已创建: 账号=admin, 密码=admin123");
        }
    }
}
