package com.hyguettelabs.medibridge.web.controller;

import com.hyguettelabs.medibridge.security.CurrentUser;
import com.hyguettelabs.medibridge.service.AuthService;
import com.hyguettelabs.medibridge.web.dto.ApiResponse;
import com.hyguettelabs.medibridge.web.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ApiResponse.ok("Account created", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout(CurrentUser.require());
        return ApiResponse.ok("Logged out", null);
    }

    @GetMapping("/me")
    public ApiResponse<AuthDtos.UserResponse> me() {
        return ApiResponse.ok(authService.me(CurrentUser.require()));
    }
}
