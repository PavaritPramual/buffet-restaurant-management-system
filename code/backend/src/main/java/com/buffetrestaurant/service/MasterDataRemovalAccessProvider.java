package com.buffetrestaurant.service;

/** Manager authorization for removal, archive reads and restore, also for direct service calls. */
public interface MasterDataRemovalAccessProvider {
    void requireManagerAccess();
}
