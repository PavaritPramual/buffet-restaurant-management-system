package com.buffetrestaurant.dto.request;

import com.buffetrestaurant.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 120) String displayName,
        @Size(max = 254) String email,
        @NotNull UserRole role
) {
}