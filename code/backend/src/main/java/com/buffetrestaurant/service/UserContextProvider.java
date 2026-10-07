package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Reads trusted server-session identity. Success has a positive userId, nonblank
 * username and non-null role; displayName is presentation data, not authorization.
 * Missing/malformed identity throws AuthenticationRequiredException (401).
 * A complete identity outside the allowed roles throws RoleAccessDeniedException (403).
 * No caller-supplied role header can establish identity or add permissions.
 */
public interface UserContextProvider {
    UserContext requireAuthenticated(HttpServletRequest request);
    UserContext requireAnyRole(HttpServletRequest request, UserRole... roles);
    UserContext requireCurrentRequestRole(UserRole... roles);
}
