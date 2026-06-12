package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.AdminCreateUserRequest;
import com.example.hackathonseal.models.dto.request.AccountStatusRequest;
import com.example.hackathonseal.models.dto.response.UserAdminResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    UserAdminResponse createUser(AdminCreateUserRequest request);

    UserAdminResponse updateStatus(Long id, AccountStatusRequest request);

    Page<UserAdminResponse> listUsers(Pageable pageable);

    UserAdminResponse getUser(Long id);
}

