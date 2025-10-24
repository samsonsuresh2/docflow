package com.docflow.docflow_core.controller;

import com.docflow.docflow_core.entity.UserAccount;
import com.docflow.docflow_core.model.LoginRequest;
import com.docflow.docflow_core.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public UserAccount login(@RequestBody LoginRequest request) {
        return authService.validate(request.getUsername(), request.getPassword());
    }
}
