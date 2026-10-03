package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.exception.ForbiddenException;
import com.buffetrestaurant.exception.UnauthorizedException;
import com.buffetrestaurant.service.OrderFulfillmentAccessProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Development/test fixture only. Reads the caller's role from an {@code X-User-Role} header
 * (e.g. {@code KITCHEN_STAFF}) instead of a real session/JWT, since the Authentication module has
 * not shipped yet. {@code SUPERVISOR} and {@code MANAGER} are treated as able to act on both
 * boards. Never enable this provider in a deployed environment.
 */
@Component
@Profile({"local", "test", "demo"})
@ConditionalOnProperty(name = "app.fulfillment.access-provider", havingValue = "fixture")
public class FixtureOrderFulfillmentAccessProvider implements OrderFulfillmentAccessProvider {

    static final String ROLE_HEADER = "X-User-Role";

    private static final Set<UserRole> KITCHEN_ROLES =
            EnumSet.of(UserRole.KITCHEN_STAFF, UserRole.SUPERVISOR, UserRole.MANAGER);
    private static final Set<UserRole> SERVICE_STAFF_ROLES =
            EnumSet.of(UserRole.SERVICE_STAFF, UserRole.SUPERVISOR, UserRole.MANAGER);

    private final HttpServletRequest request;

    public FixtureOrderFulfillmentAccessProvider(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public void requireKitchenAccess() {
        requireOneOf(KITCHEN_ROLES);
    }

    @Override
    public void requireServiceStaffAccess() {
        requireOneOf(SERVICE_STAFF_ROLES);
    }

    private void requireOneOf(Set<UserRole> allowedRoles) {
        UserRole role = currentRole();
        if (!allowedRoles.contains(role)) {
            throw new ForbiddenException("Role " + role + " is not permitted to perform this action");
        }
    }

    private UserRole currentRole() {
        String header = request.getHeader(ROLE_HEADER);
        if (header == null || header.isBlank()) {
            throw new UnauthorizedException("Missing " + ROLE_HEADER + " header; caller is not authenticated");
        }
        try {
            return UserRole.valueOf(header.trim());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Unknown staff role in " + ROLE_HEADER + " header: " + header);
        }
    }
}
