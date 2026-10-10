package com.buffetrestaurant.service.state;

import com.buffetrestaurant.domain.enums.OrderStatus;

/** Initial state: the kitchen has not started preparing the order yet. */
public final class ReceivedState implements OrderState {

    public static final ReceivedState INSTANCE = new ReceivedState();

    private ReceivedState() {
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.RECEIVED;
    }

    @Override
    public OrderState next() {
        return PreparingState.INSTANCE;
    }
}
