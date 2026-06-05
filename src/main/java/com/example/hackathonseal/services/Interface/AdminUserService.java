package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.AdminCreateUserRequest;
import com.example.hackathonseal.models.dto.request.AccountStatusRequest;
import com.example.hackathonseal.models.dto.response.UserAdminResponse;

public interface AdminUserService {
    UserAdminResponse createUser(AdminCreateUserRequest request);

    UserAdminResponse updateStatus(Long id, AccountStatusRequest request);
}

