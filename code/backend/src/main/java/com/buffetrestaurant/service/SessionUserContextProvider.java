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
    private final AuthenticationService accounts;

    public SessionUserContextProvider(AuthenticationService accounts) {
        this.accounts = accounts;
    }

    public UserContext requireAuthenticated(HttpServletRequest request) {
        if (request == null) throw new AuthenticationRequiredException();
        HttpSession session = request.getSession(false);
        Object context = session == null ? null : session.getAttribute(UserSessionKeys.USER_CONTEXT_SESSION_KEY);
        if (context instanceof UserContext userContext
                && userContext.userId() != null && userContext.userId() > 0
                && userContext.username() != null && !userContext.username().isBlank()
                && userContext.role() != null) {
            // Fallback when session revocation raced or failed: the stored account must still be usable.
            if (accounts.isLoginAllowed(userContext.userId())) return userContext;
            discard(session);
        }
        throw new AuthenticationRequiredException();
    }

    private static void discard(HttpSession session) {
        try {
            session.removeAttribute(UserSessionKeys.USER_CONTEXT_SESSION_KEY);
            session.invalidate();
        } catch (IllegalStateException alreadyInvalid) {
            // already gone
        }
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
        if (roles == null || Arrays.stream(roles).noneMatch(role -> role != null && role == user.role())) {
            throw new RoleAccessDeniedException("Your role is not allowed to perform this operation");
        }
    }
}
