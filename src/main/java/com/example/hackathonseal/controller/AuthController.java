package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.LoginRequest;
import com.example.hackathonseal.models.dto.request.RegisterRequest;
import com.example.hackathonseal.models.dto.response.AuthResponse;
import com.example.hackathonseal.models.dto.response.RegisterResponse;
import com.example.hackathonseal.services.Interface.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}

