package com.buffetrestaurant.service.state;

import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.exception.BusinessRuleException;

/** Terminal state: the order has been served and can never transition again. */
public final class ServedState implements OrderState {

    public static final ServedState INSTANCE = new ServedState();

    private ServedState() {
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.SERVED;
    }

    @Override
    public OrderState next() {
        throw new BusinessRuleException("Order has already been served; no further status transitions are allowed");
    }
}
