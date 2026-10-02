package com.buffetrestaurant.service.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

class OrderStateTest {

    @Test
    void forStatus_whenReceived_returnsReceivedStateThatAdvancesToPreparing() {
        OrderState state = OrderStateFactory.forStatus(OrderStatus.RECEIVED);

        assertThat(state.status()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(state.next().status()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void forStatus_whenPreparing_returnsPreparingStateThatAdvancesToReady() {
        OrderState state = OrderStateFactory.forStatus(OrderStatus.PREPARING);

        assertThat(state.status()).isEqualTo(OrderStatus.PREPARING);
        assertThat(state.next().status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void forStatus_whenReady_returnsReadyStateThatAdvancesToServed() {
        OrderState state = OrderStateFactory.forStatus(OrderStatus.READY);

        assertThat(state.status()).isEqualTo(OrderStatus.READY);
        assertThat(state.next().status()).isEqualTo(OrderStatus.SERVED);
    }

    @Test
    void forStatus_whenServed_returnsTerminalStateThatRejectsFurtherTransitions() {
        OrderState state = OrderStateFactory.forStatus(OrderStatus.SERVED);

        assertThat(state.status()).isEqualTo(OrderStatus.SERVED);
        assertThatThrownBy(state::next)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already been served");
    }
}
