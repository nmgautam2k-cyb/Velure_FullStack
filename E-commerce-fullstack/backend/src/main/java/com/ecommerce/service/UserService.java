package com.ecommerce.service;

import com.ecommerce.dto.AdminCreateUserRequest;
import com.ecommerce.dto.AdminUpdateUserRequest;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.UserResponse;

/** Administrative user management (all endpoints are ADMIN-only). */
public interface UserService {

    PageResponse<UserResponse> list(int page, int size);

    UserResponse create(AdminCreateUserRequest request);

    UserResponse update(Long id, AdminUpdateUserRequest request, Long actingAdminId);

    void delete(Long id, Long actingAdminId);
}
