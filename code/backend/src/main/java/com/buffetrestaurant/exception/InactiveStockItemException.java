package com.buffetrestaurant.exception;

public class InactiveStockItemException extends StockRuleViolationException {
    public InactiveStockItemException() {
        super("Stock item is inactive; activate it before stock-in or adjustment");
    }
}
