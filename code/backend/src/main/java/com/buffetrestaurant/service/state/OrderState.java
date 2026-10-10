package com.buffetrestaurant.service.state;

import com.buffetrestaurant.domain.enums.OrderStatus;

/**
 * State Pattern participant for {@link OrderStatus} fulfillment transitions.
 *
 * <p>Each concrete state knows the single {@link OrderStatus} it represents and the
 * single next state that legally follows it. There is deliberately no way to jump to an
 * arbitrary target status: callers ask a state for {@link #next()} and compare the result
 * against what was requested, which is what makes skipping or reversing a transition
 * impossible by construction rather than by a checklist of forbidden pairs.
 */
public interface OrderState {

    /** The {@link OrderStatus} this state represents. */
    OrderStatus status();

    /**
     * The state that legally follows this one.
     *
     * @throws com.buffetrestaurant.exception.BusinessRuleException if this state is terminal
     *         and has no legal next state (i.e. {@link OrderStatus#SERVED}).
     */
    OrderState next();
}
