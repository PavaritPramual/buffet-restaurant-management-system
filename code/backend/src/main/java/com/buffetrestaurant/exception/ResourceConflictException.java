package com.buffetrestaurant.exception;

/** A request conflicts with the current state of a resource; mapped to HTTP 409 with no partial write. */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
