package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import jakarta.servlet.http.HttpServletRequest;

/** Reads trusted server-session identity and enforces the requested roles. */
public interface UserContextProvider {
    UserContext requireAuthenticated(HttpServletRequest request);
    UserContext requireAnyRole(HttpServletRequest request, UserRole... roles);
    UserContext requireCurrentRequestRole(UserRole... roles);
}
