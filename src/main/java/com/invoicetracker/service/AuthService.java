package com.invoicetracker.service;

import com.invoicetracker.dto.request.LoginRequest;
import com.invoicetracker.dto.request.RegisterRequest;
import com.invoicetracker.dto.response.AuthResponse;
import com.invoicetracker.dto.response.UserResponse;
import com.invoicetracker.exception.BadRequestException;
import com.invoicetracker.model.Userr;
import com.invoicetracker.model.UserSettings;
import com.invoicetracker.model.enums.SubscriptionTier;
import com.invoicetracker.repository.UserRepository;
import com.invoicetracker.repository.UserSettingsRepository;
import com.invoicetracker.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        Userr user = Userr.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .companyName(request.getCompanyName())
                .phone(request.getPhone())
                .subscriptionTier(SubscriptionTier.FREE)
                .trialEndsAt(LocalDateTime.now().plusDays(14))
                .build();

        user = userRepository.save(user);

        // Create default settings
        UserSettings settings = UserSettings.builder()
                .user(user)
                .build();
        userSettingsRepository.save(settings);

        // Auto login
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.of(jwt, 86400000L, UserResponse.fromEntity(user));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        Userr user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return AuthResponse.of(jwt, 86400000L, UserResponse.fromEntity(user));
    }
}