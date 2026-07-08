package com.example.xiamenbackground.repository;

import com.example.xiamenbackground.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByName(String name);
    boolean existsByName(String name);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    long countByRole(String role);
}
