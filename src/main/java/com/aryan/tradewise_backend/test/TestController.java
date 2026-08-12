package com.aryan.tradewise_backend.test;

import com.aryan.tradewise_backend.security.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final CurrentUserService currentUserService;

    public TestController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/api/test/current-user")
    public String currentUser() {
        return currentUserService.getCurrentUserEmail();
    }

    @GetMapping("/api/test")
    public String test() {
        return "You are authenticated!";
    }
}