package com.buffetrestaurant.exception;

public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException() {
        super("A signed-in user is required");
    }
}