package com.buffetrestaurant.service;

/**
 * Authorization seam for menu catalog mutations.
 *
 * <p>The Authentication module must provide the production implementation and
 * verify the current staff role before returning from this method.</p>
 */
public interface MenuAdminAccessProvider {
    void requireMenuWriteAccess();
}
