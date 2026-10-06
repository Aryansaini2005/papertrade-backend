package com.aryan.tradewise_backend.admin.controller;

import com.aryan.tradewise_backend.user.entity.User;
import com.aryan.tradewise_backend.user.enums.Status;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/count")
    public long getUserCount() {
        return userRepository.count();
    }

    @PatchMapping("/{userId}/status")
    public User updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Status status) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(status);

        return userRepository.save(user);
    }
}