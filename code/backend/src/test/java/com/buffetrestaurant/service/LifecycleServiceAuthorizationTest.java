package com.buffetrestaurant.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.exception.AuthenticationRequiredException;
import com.buffetrestaurant.exception.RoleAccessDeniedException;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.repository.UserProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class LifecycleServiceAuthorizationTest {
    private final UserAccountRepository accounts = mock(UserAccountRepository.class);
    private final UserProfileRepository profiles = mock(UserProfileRepository.class);
    private final StockItemRepository items = mock(StockItemRepository.class);
    private final StockTransactionRepository transactions = mock(StockTransactionRepository.class);
    private final AuthenticationService authentication = mock(AuthenticationService.class);
    private final UserContextProvider access = new SessionUserContextProvider(authentication);
    private final UserLifecycleService lifecycle = new UserLifecycleService(accounts, profiles, transactions,
            mock(StaffSessionRegistry.class), access);
    private final StockService stock = new StockService(items, transactions, null, null, access);

    @AfterEach
    void clearRequest() { RequestContextHolder.resetRequestAttributes(); }

    private void loginAs(UserRole role) {
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute("userContext", new com.buffetrestaurant.dto.response.UserContext(5L, "u", "U", role));
        org.mockito.Mockito.when(authentication.isLoginAllowed(5L)).thenReturn(true);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void assertAll(Class<? extends Throwable> expected) {
        assertThatThrownBy(() -> lifecycle.remove(1L)).isInstanceOf(expected);
        assertThatThrownBy(() -> lifecycle.restore(1L)).isInstanceOf(expected);
        assertThatThrownBy(lifecycle::listArchived).isInstanceOf(expected);
        assertThatThrownBy(() -> lifecycle.setActive(1L, false)).isInstanceOf(expected);
        assertThatThrownBy(() -> stock.removeItem(1L)).isInstanceOf(expected);
        assertThatThrownBy(() -> stock.restoreItem(1L)).isInstanceOf(expected);
        assertThatThrownBy(stock::archivedItems).isInstanceOf(expected);
        verifyNoInteractions(accounts, items, transactions);
    }

    @Test
    void rejectsCallsWithoutLogin() { assertAll(AuthenticationRequiredException.class); }

    @Test
    void rejectsNonManagerRoles() {
        for (UserRole role : UserRole.values()) {
            if (role == UserRole.MANAGER) continue;
            loginAs(role);
            assertAll(RoleAccessDeniedException.class);
        }
    }
}
