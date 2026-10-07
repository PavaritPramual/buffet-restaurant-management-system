package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;

public interface BillingContextProvider {
    /**
     * Returns a non-null context matching the positive requested ID, including
     * package price snapshot, guest counts and status. Throws when missing or
     * unavailable. A null discountContext means no promotion; it is not an error.
     */
    BillingContext findBySessionId(Long sessionId);
}
