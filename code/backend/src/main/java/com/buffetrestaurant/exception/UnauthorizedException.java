package com.buffetrestaurant.exception;

/** No valid caller identity was presented at all (e.g. missing/unparseable staff role). */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
