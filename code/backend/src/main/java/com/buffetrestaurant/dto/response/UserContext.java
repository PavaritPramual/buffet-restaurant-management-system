package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.UserRole;

public record UserContext(Long userId, String username, String displayName, UserRole role) {
}