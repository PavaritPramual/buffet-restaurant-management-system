package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.exception.ServiceUnavailableException;
import com.buffetrestaurant.service.fixture.DisabledSessionContextProvider;
import com.buffetrestaurant.service.fixture.FixtureSessionContextProvider;
import com.buffetrestaurant.service.impl.DatabaseSessionContextProvider;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionContextProviderContractTest {
    @Test
    void databaseAdapterDelegatesOrderToLockingVerificationRatherThanRead() {
        var verifier = mock(CustomerSessionVerifier.class);
        SessionContextProvider provider = new DatabaseSessionContextProvider(verifier);
        var context = new SessionContextProvider.SessionContextSnapshot(12L, 2L, "T12", DiningSessionStatus.ACTIVE);
        when(verifier.requireSessionForOrder(12L, "test-credential")).thenReturn(context);
        assertThat(provider.requireSessionForOrder(12L, "test-credential")).isSameAs(context);
        verify(verifier).requireSessionForOrder(12L, "test-credential");
        verify(verifier, never()).requireSession(any(), any());
    }

    @Test
    void fixtureHasExplicitSyntheticContractAndDoesNotRepresentDatabaseLockEvidence() {
        SessionContextProvider provider = new FixtureSessionContextProvider();
        var read = provider.requireSession(1L, "fixture-active-token");
        assertThat(provider.requireSessionForOrder(1L, "fixture-active-token")).isEqualTo(read);
        assertThat(read.status()).isEqualTo(DiningSessionStatus.ACTIVE);
        assertThatThrownBy(() -> provider.requireSession(2L, "fixture-active-token"))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> provider.requireSessionForOrder(1L, "invalid"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void disabledProviderAlwaysFailsClosedForReadAndOrder() {
        SessionContextProvider provider = new DisabledSessionContextProvider();
        assertThatThrownBy(() -> provider.requireSession(1L, "test-credential"))
                .isInstanceOf(ServiceUnavailableException.class);
        assertThatThrownBy(() -> provider.requireSessionForOrder(1L, "test-credential"))
                .isInstanceOf(ServiceUnavailableException.class);
    }
}
