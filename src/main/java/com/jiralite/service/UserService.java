package com.jiralite.service;

import com.jiralite.domain.AuditEventType;
import com.jiralite.domain.Role;
import com.jiralite.domain.User;
import com.jiralite.dto.AuthRequest;
import com.jiralite.dto.AuthResponse;
import com.jiralite.dto.RegisterRequest;
import com.jiralite.dto.UserResponse;
import com.jiralite.exception.BadRequestException;
import com.jiralite.exception.ResourceNotFoundException;
import com.jiralite.repository.UserRepository;
import com.jiralite.security.JwtTokenService;
import com.jiralite.security.SecurityService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final SecurityService securityService;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService,
                       SecurityService securityService,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.securityService = securityService;
        this.auditLogService = auditLogService;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.DEVELOPER;
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        User saved = userRepository.save(user);

        auditLogService.log(
                AuditEventType.USER_REGISTERED,
                saved.getUsername(),
                "USER",
                saved.getId(),
                null,
                null,
                "User registered with role " + saved.getRole()
        );

        return UserResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtTokenService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserResponse changeUserRole(Long userId, Role newRole) {
        securityService.requireAdmin();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role oldRole = user.getRole();
        user.setRole(newRole);
        User updated = userRepository.save(user);

        User actor = securityService.getCurrentUser();
        auditLogService.log(
                AuditEventType.USER_ROLE_CHANGED,
                actor.getUsername(),
                "USER",
                updated.getId(),
                null,
                null,
                "Role changed from " + oldRole + " to " + newRole
        );

        return UserResponse.fromEntity(updated);
    }
}
