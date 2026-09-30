package com.buffetrestaurant.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class BootstrapAdminConfigTest {
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final AuthService authService = mock(AuthService.class);
    private final BootstrapAdminConfig config = new BootstrapAdminConfig();

    @Test
    void createsManagerOnlyWhenDatabaseHasNoUsers() throws Exception {
        when(users.count()).thenReturn(0L);

        config.bootstrapInitialManager(users, authService, "first-manager", "initial-secret",
                "Restaurant Manager", "manager@example.test").run(new DefaultApplicationArguments());

        verify(authService).createUser(new CreateUserRequest("first-manager", "initial-secret",
                "Restaurant Manager", "manager@example.test", UserRole.MANAGER));
    }

    @Test
    void doesNothingAfterUsersExistAndDelegatesCredentialValidationToAuthService() throws Exception {
        when(users.count()).thenReturn(1L);
        config.bootstrapInitialManager(users, authService, "first-manager", "initial-secret",
                "Restaurant Manager", "").run(new DefaultApplicationArguments());
        verify(authService, never()).createUser(any(CreateUserRequest.class));

        when(users.count()).thenReturn(0L);
        config.bootstrapInitialManager(users, authService, "first-manager", "short",
                "Restaurant Manager", "").run(new DefaultApplicationArguments());
        verify(authService).createUser(new CreateUserRequest("first-manager", "short",
                "Restaurant Manager", "", UserRole.MANAGER));
    }
}