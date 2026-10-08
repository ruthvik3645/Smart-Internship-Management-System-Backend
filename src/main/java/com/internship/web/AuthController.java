package com.internship.web;

import com.internship.dto.Dtos.*;
import com.internship.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest r) { return auth.register(r); }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest r) { return auth.login(r); }
}
