package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.repository.PaymentRepository;
import com.buffetrestaurant.service.billing.BillCalculator;
import com.buffetrestaurant.service.billing.BillingContextProvider;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerBillingContractTest {
    static Stream<BillingContext> invalidContexts() {
        var mismatch = new BillingContext();
        mismatch.setSessionId(99L);
        mismatch.setSessionStatus(DiningSessionStatus.ACTIVE);
        var closed = new BillingContext();
        closed.setSessionId(1L);
        closed.setSessionStatus(DiningSessionStatus.COMPLETED);
        return Stream.of(null, mismatch, closed);
    }

    @ParameterizedTest
    @MethodSource("invalidContexts")
    void refusesInvalidProviderResultBeforeCalculation(BillingContext context) {
        var access = mock(CustomerSessionVerifier.class);
        var sessions = mock(DiningSessionRepository.class);
        var payments = mock(PaymentRepository.class);
        var provider = mock(BillingContextProvider.class);
        var calculator = mock(BillCalculator.class);
        var service = new CustomerBillingService(access, sessions, payments, provider, calculator, mock(EntityManager.class));
        when(sessions.findById(1L)).thenReturn(Optional.of(mock(DiningSession.class)));
        when(payments.findBySessionId(1L)).thenReturn(Optional.empty());
        when(provider.findBySessionId(1L)).thenReturn(context);
        assertThatThrownBy(() -> service.status(1L, "test-credential"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Billing context");
        verifyNoInteractions(calculator);
    }
}
