package com.buffetrestaurant.service.state;

import com.buffetrestaurant.config.OrderStateConfig;
import com.buffetrestaurant.domain.enums.OrderStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class OrderStateResolverTest {
    private static List<OrderState> states() {
        return List.of(ReceivedState.INSTANCE, PreparingState.INSTANCE, ReadyState.INSTANCE, ServedState.INSTANCE);
    }

    @Test
    void resolvesEveryStateAndDoesNotRetainMutableRegistrationList() {
        var registration = new ArrayList<>(states());
        OrderStateResolver resolver = new RegistryOrderStateResolver(registration);
        registration.clear();
        for (OrderState state : states()) assertThat(resolver.resolve(state.status())).isSameAs(state);
        assertThatIllegalArgumentException().isThrownBy(() -> resolver.resolve(null));
    }

    @Test
    void acceptsAnAlternativePolicyWithoutChangingResolverCode() {
        OrderState alternate = new OrderState() {
            public OrderStatus status() { return OrderStatus.RECEIVED; }
            public OrderState next() { return PreparingState.INSTANCE; }
        };
        var resolver = new RegistryOrderStateResolver(List.of(alternate, PreparingState.INSTANCE,
                ReadyState.INSTANCE, ServedState.INSTANCE));
        assertThat(resolver.resolve(OrderStatus.RECEIVED)).isSameAs(alternate);
    }

    @Test
    void rejectsDuplicateMissingAndNullRegistration() {
        var duplicate = new ArrayList<>(states());
        duplicate.add(ReceivedState.INSTANCE);
        assertThatIllegalArgumentException().isThrownBy(() -> new RegistryOrderStateResolver(duplicate))
                .withMessageContaining("Duplicate");
        assertThatIllegalArgumentException().isThrownBy(() -> new RegistryOrderStateResolver(List.of(ReceivedState.INSTANCE)))
                .withMessageContaining("Missing");
        assertThatIllegalArgumentException().isThrownBy(() -> new RegistryOrderStateResolver(null));
        assertThatIllegalArgumentException().isThrownBy(() -> new RegistryOrderStateResolver(Arrays.asList((OrderState) null)));
        OrderState noStatus = new OrderState() {
            public OrderStatus status() { return null; }
            public OrderState next() { return PreparingState.INSTANCE; }
        };
        assertThatIllegalArgumentException().isThrownBy(() -> new RegistryOrderStateResolver(List.of(noStatus)));
    }

    @Test
    void springRegistersTheExistingSingletons() {
        new ApplicationContextRunner().withUserConfiguration(OrderStateConfig.class)
                .withBean(RegistryOrderStateResolver.class).run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(OrderStateResolver.class);
                    assertThat(context.getBean(OrderStateResolver.class).resolve(OrderStatus.READY))
                            .isSameAs(ReadyState.INSTANCE);
                    assertThat(context.getBeansOfType(OrderState.class)).hasSize(4);
                });
    }

    @Test
    void duplicateRegistrationFailsStartupRatherThanUsingAnArbitraryBean() {
        new ApplicationContextRunner().withUserConfiguration(OrderStateConfig.class)
                .withBean("duplicateReceived", OrderState.class, () -> ReceivedState.INSTANCE)
                .withBean(RegistryOrderStateResolver.class).run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .hasStackTraceContaining("Duplicate order state");
                });
    }

    @Test
    void incompleteRegistrationFailsStartup() {
        new ApplicationContextRunner().withBean("received", OrderState.class, () -> ReceivedState.INSTANCE)
                .withBean(RegistryOrderStateResolver.class).run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .hasStackTraceContaining("Missing order states");
                });
    }
}
