package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.Disclaimer;
import com.hyguettelabs.medibridge.domain.entity.PatientProfile;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.domain.enums.Role;
import com.hyguettelabs.medibridge.repository.PatientProfileRepository;
import com.hyguettelabs.medibridge.repository.UserAccountRepository;
import com.hyguettelabs.medibridge.security.JwtService;
import com.hyguettelabs.medibridge.web.dto.AuthDtos;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository users;
    private final PatientProfileRepository profiles;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AccessAuditService audit;

    public AuthService(UserAccountRepository users,
                       PatientProfileRepository profiles,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       AccessAuditService audit) {
        this.users = users;
        this.profiles = profiles;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.audit = audit;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("An account with that email already exists");
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(request.password()));
        user.setRole(Role.PATIENT);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(request.phone());
        users.save(user);

        PatientProfile profile = new PatientProfile();
        profile.setUser(user);
        profiles.save(profile);

        audit.record(email, "REGISTER", "UserAccount", user.getId().toString(), "patient self-registration");
        return toAuth(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!user.isEnabled() || !encoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        audit.record(user, user.getRole() == Role.PATIENT ? user.getId() : null,
                "LOGIN", "UserAccount", user.getId().toString(), null);
        return toAuth(user);
    }

    @Transactional
    public void logout(UserAccount user) {
        user.bumpTokenVersion();
        users.save(user);
        audit.record(user, user.getRole() == Role.PATIENT ? user.getId() : null,
                "LOGOUT", "UserAccount", user.getId().toString(), "token version incremented");
    }

    public AuthDtos.UserResponse me(UserAccount user) {
        return toUser(user);
    }

    private AuthDtos.AuthResponse toAuth(UserAccount user) {
        return new AuthDtos.AuthResponse(
                jwtService.issue(user),
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                Disclaimer.TEXT
        );
    }

    static AuthDtos.UserResponse toUser(UserAccount user) {
        return new AuthDtos.UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.isEnabled(),
                user.getCreatedAt(),
                Disclaimer.TEXT
        );
    }
}
