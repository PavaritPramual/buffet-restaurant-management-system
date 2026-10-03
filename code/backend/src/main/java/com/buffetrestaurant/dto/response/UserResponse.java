package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.UserRole;

public record UserResponse(Long id, String username, String displayName, String email, UserRole role) {
}