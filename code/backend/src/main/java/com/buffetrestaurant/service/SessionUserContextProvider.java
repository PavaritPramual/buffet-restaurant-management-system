package com.buffetrestaurant.service;

import com.buffetrestaurant.common.UserSessionKeys;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.exception.AuthenticationRequiredException;
import com.buffetrestaurant.exception.RoleAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SessionUserContextProvider implements UserContextProvider {

    public UserContext requireAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object context = session == null ? null : session.getAttribute(UserSessionKeys.USER_CONTEXT_SESSION_KEY);
        if (context instanceof UserContext userContext) return userContext;
        throw new AuthenticationRequiredException();
    }

    public UserContext requireAnyRole(HttpServletRequest request, UserRole... roles) {
        UserContext user = requireAuthenticated(request);
        requireRole(user, roles);
        return user;
    }

    public UserContext requireCurrentRequestRole(UserRole... roles) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            throw new AuthenticationRequiredException();
        }
        UserContext user = requireAuthenticated(attributes.getRequest());
        requireRole(user, roles);
        return user;
    }

    private void requireRole(UserContext user, UserRole... roles) {
        if (Arrays.stream(roles).noneMatch(role -> role == user.role())) {
            throw new RoleAccessDeniedException("Your role is not allowed to perform this operation");
        }
    }
}
