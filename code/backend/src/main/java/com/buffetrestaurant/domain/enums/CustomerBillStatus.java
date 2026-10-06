package com.buffetrestaurant.domain.enums;

/** Bill flow only; PAID does not change DiningSessionStatus or close the table. */
public enum CustomerBillStatus {
    NOT_REQUESTED,
    REQUESTED,
    PAID
}
