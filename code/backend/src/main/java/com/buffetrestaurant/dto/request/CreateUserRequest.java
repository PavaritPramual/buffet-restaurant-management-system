package com.buffetrestaurant.dto.request;

import com.buffetrestaurant.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 80)
        @Schema(description = "Unique staff login name", example = "staff01")
        String username,
        @NotBlank @Size(min = 8, max = 72)
        @Schema(description = "Initial password; never returned in API responses", example = "********")
        String password,
        @NotBlank @Size(max = 120)
        @Schema(description = "Name displayed in staff UI", example = "Service Staff")
        String displayName,
        @Email @Size(max = 254)
        @Schema(description = "Optional email address", example = "staff@example.test", nullable = true)
        String email,
        @NotNull
        @Schema(description = "Staff role", example = "SERVICE_STAFF")
        UserRole role
) {
}