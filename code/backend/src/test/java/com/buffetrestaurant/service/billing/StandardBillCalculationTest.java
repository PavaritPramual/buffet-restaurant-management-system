package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StandardBillCalculationTest {

    @Test
    void calculate_whenTwoAdultsAndOneChild_returns997Point50() {
        // Given
        BillingContext context = new BillingContext();
        context.setPackagePrice(new BigDecimal("399.00"));
        context.setAdultCount(2);
        context.setChildCount(1);

        StandardBillCalculation calculator = new StandardBillCalculation();

        // When
        BigDecimal result = calculator.calculate(context);

        // Then
        assertThat(result).isEqualByComparingTo("997.50");
    }

    @Test
    void calculate_whenContextIsNull_throwsIllegalArgumentException() {
        StandardBillCalculation calculator = new StandardBillCalculation();

        assertThatThrownBy(() -> calculator.calculate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Billing context is required");
    }
}