package com.buffetrestaurant.service.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

class OrderStateTest {
    private final OrderStateResolver resolver = new RegistryOrderStateResolver(java.util.List.of(ReceivedState.INSTANCE, PreparingState.INSTANCE, ReadyState.INSTANCE, ServedState.INSTANCE));

    @Test
    void forStatus_whenReceived_returnsReceivedStateThatAdvancesToPreparing() {
        OrderState state = resolver.resolve(OrderStatus.RECEIVED);

        assertThat(state.status()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(state.next().status()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void forStatus_whenPreparing_returnsPreparingStateThatAdvancesToReady() {
        OrderState state = resolver.resolve(OrderStatus.PREPARING);

        assertThat(state.status()).isEqualTo(OrderStatus.PREPARING);
        assertThat(state.next().status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void forStatus_whenReady_returnsReadyStateThatAdvancesToServed() {
        OrderState state = resolver.resolve(OrderStatus.READY);

        assertThat(state.status()).isEqualTo(OrderStatus.READY);
        assertThat(state.next().status()).isEqualTo(OrderStatus.SERVED);
    }

    @Test
    void forStatus_whenServed_returnsTerminalStateThatRejectsFurtherTransitions() {
        OrderState state = resolver.resolve(OrderStatus.SERVED);

        assertThat(state.status()).isEqualTo(OrderStatus.SERVED);
        assertThatThrownBy(state::next)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already been served");
    }
}
