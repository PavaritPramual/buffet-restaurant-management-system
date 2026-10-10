package com.buffetrestaurant.service.state;

import com.buffetrestaurant.domain.enums.OrderStatus;

/** The kitchen has started preparing the order. */
public final class PreparingState implements OrderState {

    public static final PreparingState INSTANCE = new PreparingState();

    private PreparingState() {
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PREPARING;
    }

    @Override
    public OrderState next() {
        return ReadyState.INSTANCE;
    }
}
