package com.buffetrestaurant.service;

/** Authentication module seam for staff-only table and session operations. */
public interface DiningSessionStaffAccessProvider {
    void requireServiceStaffAccess();
}
