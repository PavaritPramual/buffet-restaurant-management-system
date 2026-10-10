package com.buffetrestaurant.service.fixture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.buffetrestaurant.exception.ForbiddenException;
import com.buffetrestaurant.exception.UnauthorizedException;
import com.buffetrestaurant.service.OrderFulfillmentAccessProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FixtureOrderFulfillmentAccessProviderTest {

    @Mock
    private HttpServletRequest request;

    private OrderFulfillmentAccessProvider accessProvider;

    @BeforeEach
    void setUp() {
        accessProvider = new FixtureOrderFulfillmentAccessProvider(request);
    }

    @Test
    void requireKitchenAccess_whenRoleHeaderMissing_throwsUnauthorized() {
        when(request.getHeader("X-User-Role")).thenReturn(null);

        assertThatThrownBy(() -> accessProvider.requireKitchenAccess())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("X-User-Role");
    }

    @Test
    void requireKitchenAccess_whenRoleHeaderBlank_throwsUnauthorized() {
        when(request.getHeader("X-User-Role")).thenReturn("   ");

        assertThatThrownBy(() -> accessProvider.requireKitchenAccess())
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void requireKitchenAccess_whenRoleHeaderUnknown_throwsUnauthorized() {
        when(request.getHeader("X-User-Role")).thenReturn("not_a_role");

        assertThatThrownBy(() -> accessProvider.requireKitchenAccess())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not_a_role");
    }

    @Test
    void requireKitchenAccess_whenRoleIsServiceStaff_throwsForbidden() {
        when(request.getHeader("X-User-Role")).thenReturn("SERVICE_STAFF");

        assertThatThrownBy(() -> accessProvider.requireKitchenAccess())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("SERVICE_STAFF");
    }

    @Test
    void requireKitchenAccess_whenRoleIsKitchenStaff_allowsAccess() {
        when(request.getHeader("X-User-Role")).thenReturn("KITCHEN_STAFF");

        assertThatCode(() -> accessProvider.requireKitchenAccess()).doesNotThrowAnyException();
    }

    @Test
    void supervisorAndManagerCannotUseEitherBoardEvenInFixture() {
        for (String role : new String[]{"SUPERVISOR", "MANAGER"}) {
            when(request.getHeader("X-User-Role")).thenReturn(role);
            assertThatThrownBy(() -> accessProvider.requireKitchenAccess()).isInstanceOf(ForbiddenException.class);
            assertThatThrownBy(() -> accessProvider.requireServiceStaffAccess()).isInstanceOf(ForbiddenException.class);
        }
    }

    @Test
    void requireServiceStaffAccess_whenRoleIsKitchenStaff_throwsForbidden() {
        when(request.getHeader("X-User-Role")).thenReturn("KITCHEN_STAFF");

        assertThatThrownBy(() -> accessProvider.requireServiceStaffAccess())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("KITCHEN_STAFF");
    }

    @Test
    void requireServiceStaffAccess_whenRoleIsServiceStaff_allowsAccess() {
        when(request.getHeader("X-User-Role")).thenReturn("SERVICE_STAFF");

        assertThatCode(() -> accessProvider.requireServiceStaffAccess()).doesNotThrowAnyException();
    }

    @Test
    void requireServiceStaffAccess_whenRoleHeaderMissing_throwsUnauthorized() {
        when(request.getHeader("X-User-Role")).thenReturn(null);

        assertThatThrownBy(() -> accessProvider.requireServiceStaffAccess())
                .isInstanceOf(UnauthorizedException.class);
    }
}
