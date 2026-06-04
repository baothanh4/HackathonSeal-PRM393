package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.LoginRequest;
import com.example.hackathonseal.models.dto.request.RegisterRequest;
import com.example.hackathonseal.models.dto.response.AuthResponse;
import com.example.hackathonseal.models.dto.response.RegisterResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
}
