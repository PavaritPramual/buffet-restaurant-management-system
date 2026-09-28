package com.buffetrestaurant.integration.billing;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import java.math.BigDecimal;

/** Read-only session data for the Billing module; discount rules remain with Billing. */
public interface DiningSessionBillingReader {
    BillingSnapshot requireBySessionId(Long sessionId);

    record BillingSnapshot(
            Long sessionId,
            BigDecimal packagePriceAtOpen,
            Integer adultCount,
            Integer childCount,
            DiningSessionStatus sessionStatus
    ) {
    }
}
