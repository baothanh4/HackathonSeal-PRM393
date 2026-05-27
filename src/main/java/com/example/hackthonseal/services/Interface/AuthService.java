package com.example.hackthonseal.services.Interface;

import com.example.hackthonseal.models.dto.request.LoginRequest;
import com.example.hackthonseal.models.dto.request.RegisterRequest;
import com.example.hackthonseal.models.dto.response.AuthResponse;
import com.example.hackthonseal.models.dto.response.RegisterResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
}
