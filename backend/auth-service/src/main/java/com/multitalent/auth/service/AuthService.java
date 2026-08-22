package com.multitalent.auth.service;

import com.multitalent.auth.client.TenantClient;
import com.multitalent.auth.dto.AuthResponse;
import com.multitalent.auth.dto.LoginRequest;
import com.multitalent.auth.dto.RegisterRequest;
import com.multitalent.auth.entity.Role;
import com.multitalent.auth.entity.User;
import com.multitalent.auth.repository.UserRepository;
import com.multitalent.common.event.UserLoggedInEvent;
import com.multitalent.common.event.UserRegisteredEvent;
import com.multitalent.common.exception.DuplicateResourceException;
import com.multitalent.common.exception.ResourceNotFoundException;
import com.multitalent.common.kafka.EventProducer;
import com.multitalent.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EventProducer eventProducer;
    private final TenantClient tenantClient;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Service-to-service call: confirm the tenant actually exists in tenant-service
        Map<String, Object> tenant = tenantClient.getTenantBySlug(request.getTenantSlug());
        String tenantId = (String) tenant.get("id");
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant not found: " + request.getTenantSlug());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .tenantId(tenantId)
                .role(Role.MEMBER)
                .build();

        User saved = userRepository.save(user);
        String token = generateToken(saved);

        eventProducer.publishEmailEvent(UserRegisteredEvent.builder()
                .eventType("USER_REGISTERED").tenantId(saved.getTenantId()).occurredAt(Instant.now())
                .userId(saved.getId()).fullName(saved.getFullName()).email(saved.getEmail())
                .build());
        eventProducer.publishAuditEvent(UserRegisteredEvent.builder()
                .eventType("USER_REGISTERED").tenantId(saved.getTenantId()).occurredAt(Instant.now())
                .userId(saved.getId()).fullName(saved.getFullName()).email(saved.getEmail())
                .build());
        eventProducer.publishAnalyticsEvent(UserRegisteredEvent.builder()
                .eventType("USER_REGISTERED").tenantId(saved.getTenantId()).occurredAt(Instant.now())
                .userId(saved.getId()).fullName(saved.getFullName()).email(saved.getEmail())
                .build());

        return buildAuthResponse(saved, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = generateToken(user);

        eventProducer.publishAuditEvent(UserLoggedInEvent.builder()
                .eventType("USER_LOGGED_IN").tenantId(user.getTenantId()).occurredAt(Instant.now())
                .userId(user.getId()).email(user.getEmail())
                .build());
        eventProducer.publishAnalyticsEvent(UserLoggedInEvent.builder()
                .eventType("USER_LOGGED_IN").tenantId(user.getTenantId()).occurredAt(Instant.now())
                .userId(user.getId()).email(user.getEmail())
                .build());

        return buildAuthResponse(user, token);
    }

    private String generateToken(User user) {
        return jwtUtil.generateToken(user.getEmail(), Map.of(
                "userId", user.getId(),
                "tenantId", user.getTenantId(),
                "role", user.getRole().name()
        ));
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(user.getTenantId())
                .build();
    }
}
