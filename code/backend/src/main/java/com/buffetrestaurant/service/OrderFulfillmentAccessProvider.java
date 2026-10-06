package com.buffetrestaurant.service;

/**
 * Authorization seam for order fulfillment actions.
 *
 * <p>SessionOrderFulfillmentAccessProvider enforces real login: kitchen staff prepare/ready,
 * service staff serve. The disabled provider fails closed when integration is not enabled.
 * The header-based fixture is only for isolated legacy tests, never deployed authorization.</p>
 */
public interface OrderFulfillmentAccessProvider {

    /** Throws if the current caller may not act on the kitchen board (start preparing / mark ready). */
    void requireKitchenAccess();

    /** Throws if the current caller may not act on the staff serving board (mark served). */
    void requireServiceStaffAccess();
}
