package com.ecommerce.service.impl;

import com.ecommerce.dto.AdminCreateUserRequest;
import com.ecommerce.dto.AdminUpdateUserRequest;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final WishlistRepository wishlistRepository;

    public UserServiceImpl(UserRepository userRepository, CartRepository cartRepository,
                           OrderRepository orderRepository, PasswordEncoder passwordEncoder,
                           WishlistRepository wishlistRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.wishlistRepository = wishlistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.ASC, "id"));
        return PageResponse.from(userRepository.findAll(pageable), UserResponse::from);
    }

    @Override
    @Transactional
    public UserResponse create(AdminCreateUserRequest request) {
        String email = AuthServiceImpl.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(blankToNull(request.phone()));
        user.setRole(Role.fromApiName(request.role()));
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, AdminUpdateUserRequest request, Long actingAdminId) {
        User user = getOrThrow(id);
        String email = AuthServiceImpl.normalizeEmail(request.email());
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException("An account with this email already exists");
        }
        Role newRole = Role.fromApiName(request.role());
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            if (id.equals(actingAdminId)) {
                throw new BadRequestException("You cannot remove your own admin role");
            }
            if (userRepository.countByRole(Role.ADMIN) <= 1) {
                throw new BadRequestException("At least one admin account must remain");
            }
        }
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(blankToNull(request.phone()));
        user.setRole(newRole);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id, Long actingAdminId) {
        User user = getOrThrow(id);
        if (id.equals(actingAdminId)) {
            throw new BadRequestException("You cannot delete your own account");
        }
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new BadRequestException("At least one admin account must remain");
        }
        if (orderRepository.existsByUserId(id)) {
            throw new ConflictException("This user has orders and cannot be deleted");
        }
        cartRepository.deleteByUserId(id);
        wishlistRepository.deleteByUserId(id);
        userRepository.delete(user);
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
