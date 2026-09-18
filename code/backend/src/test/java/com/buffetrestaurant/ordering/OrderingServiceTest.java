package com.buffetrestaurant.ordering;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.ordering.OrderingModels.*;
import com.buffetrestaurant.ordering.OrderingPorts.Sessions;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class OrderingServiceTest {
    private final DemoOrderingStore store = new DemoOrderingStore();
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-18T10:00:00Z"), ZoneOffset.UTC);
    private final OrderingService service = new OrderingService(store, store, store, clock);

    @Test
    void menu_whenSessionIsActive_returnsOnlyPackageItems() {
        assertEquals(3, service.menu(1).size());
        service.saveItem(null, new MenuItemRequest(1L, "Other package", true, java.util.Set.of(2L), null));
        service.saveItem(null, new MenuItemRequest(1L, "Unavailable", false, java.util.Set.of(1L), null));
        assertEquals(3, service.menu(1).size());
    }

    @Test
    void place_whenValid_createsReceivedOrderWithContractFields() {
        Order result = service.place(1, new PlaceOrderRequest(List.of(new OrderItemRequest(1L, 2))));
        assertEquals(OrderStatus.RECEIVED, result.status());
        assertEquals(1L, result.sessionId());
        assertEquals("A01", result.tableNumber());
        assertEquals(2, result.items().get(0).quantity());
        assertEquals("2026-09-18T10:00Z", result.createdAt().toString());
        assertEquals(result, service.order(result.orderId()));
    }

    @Test
    void place_whenItemOutsidePackage_rejectsWithoutSaving() {
        MenuItem other = service.saveItem(null, new MenuItemRequest(1L, "Other", true, java.util.Set.of(2L), null));
        OrderingException error = assertThrows(OrderingException.class, () ->
                service.place(1, new PlaceOrderRequest(List.of(new OrderItemRequest(other.id(), 1)))));
        assertEquals(HttpStatus.BAD_REQUEST, error.status());
        assertTrue(service.orders(1).isEmpty());
    }

    @Test
    void place_whenSessionIsInactive_rejectsWithoutSaving() {
        Sessions inactive = id -> Optional.of(new Session(id, 1L, "A01", DiningSessionStatus.COMPLETED));
        OrderingService subject = new OrderingService(store, inactive, store, clock);
        OrderingException error = assertThrows(OrderingException.class, () ->
                subject.place(1, new PlaceOrderRequest(List.of(new OrderItemRequest(1L, 1)))));
        assertEquals(HttpStatus.BAD_REQUEST, error.status());
        assertTrue(store.forSession(1).isEmpty());
    }

    @Test
    void place_whenQuantityIsZero_rejectsWithoutSaving() {
        OrderingException error = assertThrows(OrderingException.class, () ->
                service.place(1, new PlaceOrderRequest(List.of(new OrderItemRequest(1L, 0)))));
        assertEquals(HttpStatus.BAD_REQUEST, error.status());
        assertTrue(service.orders(1).isEmpty());
    }

    @Test
    void items_whenPagedAndSorted_isStable() {
        PageResponse<MenuItem> page = service.items(1, 1, "name,asc");
        assertEquals(3, page.totalElements());
        assertEquals(1, page.content().size());
    }

    @Test
    void saveItem_whenImageUrlProvided_returnsItForCustomerMenu() {
        MenuItem saved = service.saveItem(null, new MenuItemRequest(1L, "Soup", true,
                java.util.Set.of(1L), "https://example.com/soup.jpg"));
        assertEquals("https://example.com/soup.jpg", service.item(saved.id()).imageUrl());
        assertTrue(service.menu(1).stream().anyMatch(item ->
                item.id().equals(saved.id()) && "https://example.com/soup.jpg".equals(item.imageUrl())));
    }
}
