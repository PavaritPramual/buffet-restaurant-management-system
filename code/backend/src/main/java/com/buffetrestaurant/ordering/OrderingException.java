package com.buffetrestaurant.ordering;

import org.springframework.http.HttpStatus;

public class OrderingException extends RuntimeException {
    private final HttpStatus status;

    public OrderingException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() { return status; }
}
