package com.ecommerce.controller;

import com.ecommerce.dto.AdminCreateUserRequest;
import com.ecommerce.dto.AdminUpdateUserRequest;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        return userService.list(page, size);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody AdminUpdateUserRequest request,
                               @AuthenticationPrincipal UserPrincipal me) {
        return userService.update(id, request, me.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal me) {
        userService.delete(id, me.getId());
        return ResponseEntity.noContent().build();
    }
}
