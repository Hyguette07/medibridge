package com.hyguettelabs.medibridge.web.dto;

import com.hyguettelabs.medibridge.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @Size(max = 32) String phone
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String token,
            UUID userId,
            String email,
            Role role,
            String firstName,
            String lastName,
            String disclaimer
    ) {}

    public record UserResponse(
            UUID id,
            String email,
            Role role,
            String firstName,
            String lastName,
            String phone,
            boolean enabled,
            Instant createdAt,
            String disclaimer
    ) {}
}
