package com.buffetrestaurant.service.state;

import com.buffetrestaurant.domain.enums.OrderStatus;

/** The kitchen has finished preparing the order; it is waiting for service staff to serve it. */
public final class ReadyState implements OrderState {

    public static final ReadyState INSTANCE = new ReadyState();

    private ReadyState() {
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.READY;
    }

    @Override
    public OrderState next() {
        return ServedState.INSTANCE;
    }
}
