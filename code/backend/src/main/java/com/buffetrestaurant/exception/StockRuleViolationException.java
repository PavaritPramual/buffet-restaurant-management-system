package com.buffetrestaurant.exception;

public class StockRuleViolationException extends RuntimeException {
    public StockRuleViolationException(String message) {
        super(message);
    }
}