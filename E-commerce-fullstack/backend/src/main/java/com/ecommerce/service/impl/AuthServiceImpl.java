package com.ecommerce.service.impl;

import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtService;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(blankToNull(request.phone()));
        user.setRole(Role.USER); // never taken from the request
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException (-> 401) for an unknown email or a wrong password.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizeEmail(request.email()), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (request.role() != null && !request.role().isBlank()
                && Role.fromApiName(request.role()) != principal.getRole()) {
            throw new BadCredentialsException("Role mismatch");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new AuthResponse(jwtService.generateToken(principal), "Bearer", jwtService.expiresInSeconds(),
                UserResponse.from(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse currentUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
