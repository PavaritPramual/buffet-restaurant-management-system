package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
        @Schema(example = "8") Long id,
        @Schema(example = "staff01") String username,
        @Schema(example = "Service Staff") String displayName,
        @Schema(description = "Optional email address", example = "staff@example.test", nullable = true)
        String email,
        @Schema(example = "SERVICE_STAFF") UserRole role,
        @Schema(example = "Somchai", nullable = true) String firstName,
        @Schema(example = "Jaidee", nullable = true) String lastName,
        @Schema(example = "0812345678", nullable = true) String phoneNumber
) {
}
