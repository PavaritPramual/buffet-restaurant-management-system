package com.buffetrestaurant.exception;

/** Caller identity was presented, but its role does not permit this action. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
