package com.buffetrestaurant.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SharedEnumSerializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesEnumAsUppercaseJsonString() throws Exception {
        assertThat(objectMapper.writeValueAsString(OrderStatus.RECEIVED)).isEqualTo("\"RECEIVED\"");
    }

    @Test
    void rejectsUnknownAndLowercaseEnumValues() {
        assertThatThrownBy(() -> objectMapper.readValue("\"UNKNOWN\"", OrderStatus.class))
                .isInstanceOf(Exception.class);
        assertThatThrownBy(() -> objectMapper.readValue("\"received\"", OrderStatus.class))
                .isInstanceOf(Exception.class);
    }
}
