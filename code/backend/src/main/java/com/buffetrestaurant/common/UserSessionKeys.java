package com.buffetrestaurant.common;

/** Shared session storage contract. Keep the value stable for existing logins. */
public final class UserSessionKeys {
    public static final String USER_CONTEXT_SESSION_KEY = "userContext";

    private UserSessionKeys() {}
}
