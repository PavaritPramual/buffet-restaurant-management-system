package com.buffetrestaurant.service;

import com.buffetrestaurant.common.UserSessionKeys;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.exception.AuthenticationRequiredException;
import com.buffetrestaurant.exception.RoleAccessDeniedException;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.assertj.core.api.Assertions.*;

class SessionUserContextProviderTest {
    private final UserContextProvider users = new SessionUserContextProvider();

    @AfterEach
    void clearRequest() { RequestContextHolder.resetRequestAttributes(); }

    static Stream<Object> malformedIdentities() {
        return Stream.of("not-an-identity",
                new UserContext(null, "staff", "Staff", UserRole.SERVICE_STAFF),
                new UserContext(0L, "staff", "Staff", UserRole.SERVICE_STAFF),
                new UserContext(1L, null, "Staff", UserRole.SERVICE_STAFF),
                new UserContext(1L, " ", "Staff", UserRole.SERVICE_STAFF),
                new UserContext(1L, "staff", "Staff", null));
    }

    @ParameterizedTest
    @MethodSource("malformedIdentities")
    void rejectsMalformedServerSession(Object identity) {
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute(UserSessionKeys.USER_CONTEXT_SESSION_KEY, identity);
        request.addHeader("X-User-Role", "MANAGER");
        assertThatThrownBy(() -> users.requireAuthenticated(request))
                .isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test
    void missingRequestOrLoginCannotBeEstablishedByHeader() {
        assertThatThrownBy(() -> users.requireAuthenticated(null)).isInstanceOf(AuthenticationRequiredException.class);
        var request = new MockHttpServletRequest();
        request.addHeader("X-User-Role", "MANAGER");
        assertThatThrownBy(() -> users.requireAnyRole(request, UserRole.MANAGER))
                .isInstanceOf(AuthenticationRequiredException.class);
        assertThat(request.getSession(false)).isNull();
        assertThatThrownBy(() -> users.requireCurrentRequestRole(UserRole.MANAGER))
                .isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test
    void identityUsesStableSessionKeyAndOnlyItsActualRole() {
        var request = new MockHttpServletRequest();
        var context = new UserContext(3L, "staff", null, UserRole.SERVICE_STAFF);
        request.getSession().setAttribute("userContext", context);
        request.addHeader("X-User-Role", "MANAGER");
        assertThat(users.requireAnyRole(request, UserRole.SERVICE_STAFF)).isEqualTo(context);
        assertThatThrownBy(() -> users.requireAnyRole(request, UserRole.MANAGER))
                .isInstanceOf(RoleAccessDeniedException.class);
        assertThatThrownBy(() -> users.requireAnyRole(request, (UserRole[]) null))
                .isInstanceOf(RoleAccessDeniedException.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertThat(users.requireCurrentRequestRole(UserRole.SERVICE_STAFF)).isEqualTo(context);
    }
}
