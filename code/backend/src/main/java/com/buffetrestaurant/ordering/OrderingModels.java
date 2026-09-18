package com.buffetrestaurant.ordering;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.domain.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public final class OrderingModels {
    private OrderingModels() {}

    public record Category(Long id, String name) {}
    public record MenuItem(Long id, Long categoryId, String name, boolean available,
                           Set<Long> packageIds, String imageUrl) {}
    public record Session(Long id, Long packageId, String tableNumber, DiningSessionStatus status) {}
    public record OrderLine(Long menuItemId, String name, int quantity) {}
    public record Order(Long orderId, Long sessionId, String tableNumber, List<OrderLine> items,
                        OrderStatus status, OffsetDateTime createdAt) {}

    public record CategoryRequest(@NotBlank String name) {}
    public record MenuItemRequest(@NotNull @Positive Long categoryId, @NotBlank String name,
                                  boolean available, @NotEmpty Set<@NotNull @Positive Long> packageIds,
                                  @Pattern(regexp = "^(https?://[^\\s]+|/[^/\\s][^\\s]*)$",
                                           message = "must be an http(s) URL or an absolute site path") String imageUrl) {}
    public record OrderItemRequest(@NotNull @Positive Long menuItemId, @Min(1) int quantity) {}
    public record PlaceOrderRequest(@NotEmpty List<@NotNull @Valid OrderItemRequest> items) {}
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements) {}
}
