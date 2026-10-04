package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class BillingEngineTest {
    private final BillingEngine engine = new BillingEngine(new StandardBillCalculation());

    private BillingContext context(String price, int adults, int children) {
        BillingContext context = new BillingContext();
        context.setSessionId(12L);
        context.setPackagePrice(new BigDecimal(price));
        context.setAdultCount(adults);
        context.setChildCount(children);
        return context;
    }

    @ParameterizedTest
    @CsvSource({"2,0,798.00", "0,1,199.50", "2,1,997.50"})
    void calculatesAdultChildAndMixedBills(int adults, int children, String expected) {
        assertThat(engine.calculate(context("399", adults, children)).getTotalAmount())
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"0,997.50", "10,897.75", "100,0.00"})
    void supportsPercentageBoundaries(String percentage, String expected) {
        var context = context("399", 2, 1);
        context.setDiscountContext(Map.of("percentage", new BigDecimal(percentage)));
        var first = engine.calculate(context);
        var second = engine.calculate(context);
        assertThat(first.getTotalAmount()).isEqualByComparingTo(expected);
        assertThat(second.getTotalAmount()).isEqualTo(first.getTotalAmount());
        assertThat(first.getSubtotalNoneDiscount().subtract(first.getDiscountAmount())
                .add(first.getRoundingAdjustment())).isEqualByComparingTo(first.getTotalAmount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "100.01"})
    void rejectsOutOfRangeDiscounts(String percentage) {
        var context = context("399", 1, 0);
        context.setDiscountContext(Map.of("percentage", new BigDecimal(percentage)));
        assertThatIllegalArgumentException().isThrownBy(() -> engine.calculate(context));
    }

    @Test
    void rejectsMissingOrTextPercentage() {
        var context = context("399", 1, 0);
        context.setDiscountContext(Map.of());
        assertThatIllegalArgumentException().isThrownBy(() -> engine.calculate(context));
        context.setDiscountContext(Map.of("percentage", "10"));
        assertThatIllegalArgumentException().isThrownBy(() -> engine.calculate(context));
    }

    @Test
    void roundsOnceAfterDiscountAndDoesNotRetainPreviousBill() {
        var context = context("0.05", 1, 0);
        context.setDiscountContext(Map.of("percentage", 10));
        var bill = engine.calculate(context);
        assertThat(bill.getTotalBeforeRounding()).isEqualByComparingTo("0.045");
        assertThat(bill.getTotalAmount()).isEqualByComparingTo("0.05");
        assertThat(engine.calculate(context("399", 2, 1)).getTotalAmount()).isEqualByComparingTo("997.50");
        assertThat(engine.calculate(context).getTotalAmount()).isEqualTo(bill.getTotalAmount());
    }

    @ParameterizedTest
    @CsvSource({"-1,1,0", "399,-1,0", "399,1,-1", "399,0,0"})
    void rejectsInvalidPriceOrCounts(String price, int adults, int children) {
        assertThatIllegalArgumentException().isThrownBy(() -> engine.calculate(context(price, adults, children)));
    }
}
