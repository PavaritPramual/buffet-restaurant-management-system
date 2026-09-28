package com.buffetrestaurant.service;

/**
 * Authorization seam for order fulfillment actions.
 *
 * <p>The Authentication module (เมธัส) must provide the production implementation, backed by
 * real staff login, and verify the current caller's role before returning from either method.
 * Until then this is satisfied by {@code DisabledOrderFulfillmentAccessProvider} (fails closed)
 * or, for local/test use only, {@code FixtureOrderFulfillmentAccessProvider} (reads a staff role
 * from the {@code X-User-Role} header).</p>
 */
public interface OrderFulfillmentAccessProvider {

    /** Throws if the current caller may not act on the kitchen board (start preparing / mark ready). */
    void requireKitchenAccess();

    /** Throws if the current caller may not act on the staff serving board (mark served). */
    void requireServiceStaffAccess();
}
