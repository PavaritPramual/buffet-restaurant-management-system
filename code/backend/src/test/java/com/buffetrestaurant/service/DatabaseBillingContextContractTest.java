package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.integration.billing.DiningSessionBillingReader;
import com.buffetrestaurant.integration.billing.DiningSessionBillingReader.BillingSnapshot;
import com.buffetrestaurant.service.impl.DatabaseBillingContextProvider;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseBillingContextContractTest {
    static Stream<BillingSnapshot> invalidSnapshots() {
        return Stream.of(null,
                new BillingSnapshot(2L, BigDecimal.TEN, 1, 0, DiningSessionStatus.ACTIVE),
                new BillingSnapshot(1L, null, 1, 0, DiningSessionStatus.ACTIVE),
                new BillingSnapshot(1L, BigDecimal.TEN.negate(), 1, 0, DiningSessionStatus.ACTIVE),
                new BillingSnapshot(1L, BigDecimal.TEN, null, 0, DiningSessionStatus.ACTIVE),
                new BillingSnapshot(1L, BigDecimal.TEN, 1, null, DiningSessionStatus.ACTIVE),
                new BillingSnapshot(1L, BigDecimal.TEN, -1, 2, DiningSessionStatus.ACTIVE),
                new BillingSnapshot(1L, BigDecimal.TEN, 0, 0, DiningSessionStatus.ACTIVE),
                new BillingSnapshot(1L, BigDecimal.TEN, 1, 0, null));
    }

    @ParameterizedTest
    @MethodSource("invalidSnapshots")
    void rejectsIncompleteOrDifferentSessionSnapshot(BillingSnapshot snapshot) {
        var reader = mock(DiningSessionBillingReader.class);
        when(reader.requireBySessionId(1L)).thenReturn(snapshot);
        assertThatThrownBy(() -> new DatabaseBillingContextProvider(reader).findBySessionId(1L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("snapshot");
    }

    @Test
    void preservesSnapshotAndAbsenceOfPromotionWithoutRequiringActiveHere() {
        var reader = mock(DiningSessionBillingReader.class);
        when(reader.requireBySessionId(1L)).thenReturn(new BillingSnapshot(1L, new BigDecimal("399.00"),
                2, 1, DiningSessionStatus.COMPLETED));
        var context = new DatabaseBillingContextProvider(reader).findBySessionId(1L);
        assertThat(context.getSessionId()).isEqualTo(1L);
        assertThat(context.getPackagePrice()).isEqualByComparingTo("399.00");
        assertThat(context.getSessionStatus()).isEqualTo(DiningSessionStatus.COMPLETED);
        assertThat(context.getDiscountContext()).isNull();
    }
}
