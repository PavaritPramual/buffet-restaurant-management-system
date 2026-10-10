package com.buffetrestaurant.service;

/**
 * Authorization seam for menu catalog mutations.
 *
 * <p>The production session implementation requires the MANAGER role. Local and test
 * profiles may use the explicitly configured fixture implementation.</p>
 */
public interface MenuAdminAccessProvider {
    void requireMenuWriteAccess();
}
