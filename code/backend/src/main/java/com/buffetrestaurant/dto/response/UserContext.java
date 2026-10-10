package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserContext(
        @Schema(example = "8") Long userId,
        @Schema(example = "staff01") String username,
        @Schema(example = "Service Staff") String displayName,
        @Schema(example = "SERVICE_STAFF") UserRole role
) {
}