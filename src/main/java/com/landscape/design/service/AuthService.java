package com.landscape.design.service;

import com.landscape.design.domain.SubscriptionTier;
import com.landscape.design.domain.User;
import com.landscape.design.dto.AuthResponse;
import com.landscape.design.dto.LoginRequest;
import com.landscape.design.dto.RegisterRequest;
import com.landscape.design.dto.UserInfoDto;
import com.landscape.design.repository.UserRepository;
import com.landscape.design.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
        }
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setDisplayName(req.getDisplayName() != null ? req.getDisplayName().trim() : null);
        userRepository.save(user);
        subscriptionService.createDefaultFreeSubscription(user);
        String token = jwtService.createToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toUserInfo(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        }
        String token = jwtService.createToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toUserInfo(user));
    }

    private UserInfoDto toUserInfo(User user) {
        SubscriptionTier tier = subscriptionService.effectiveTier(user);
        return new UserInfoDto(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                tier.name()
        );
    }
}
