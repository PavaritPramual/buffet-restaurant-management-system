package com.buffetrestaurant.ordering;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.ordering.OrderingModels.*;
import com.buffetrestaurant.ordering.OrderingPorts.*;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderingService {
    private final Catalog catalog;
    private final Sessions sessions;
    private final Orders orders;
    private final Clock clock;

    @Autowired
    public OrderingService(Catalog catalog, Sessions sessions, Orders orders) {
        this(catalog, sessions, orders, Clock.systemDefaultZone());
    }

    OrderingService(Catalog catalog, Sessions sessions, Orders orders, Clock clock) {
        this.catalog = catalog;
        this.sessions = sessions;
        this.orders = orders;
        this.clock = clock;
    }

    public List<Category> categories() { return catalog.categories(); }
    public Category category(long id) { return catalog.category(id).orElseThrow(() -> missing("Category")); }
    public Category saveCategory(Long id, String name) {
        if (id != null) category(id);
        return catalog.saveCategory(id, name.trim());
    }
    public void deleteCategory(long id) {
        category(id);
        if (catalog.items().stream().anyMatch(item -> item.categoryId() == id)) {
            throw new OrderingException(HttpStatus.CONFLICT, "Category still has menu items");
        }
        catalog.deleteCategory(id);
    }

    public MenuItem item(long id) { return catalog.item(id).orElseThrow(() -> missing("Menu item")); }
    public MenuItem saveItem(Long id, MenuItemRequest request) {
        if (id != null) item(id);
        category(request.categoryId());
        return catalog.saveItem(new MenuItem(id, request.categoryId(), request.name().trim(),
                request.available(), Set.copyOf(request.packageIds())));
    }
    public void deleteItem(long id) {
        item(id);
        catalog.deleteItem(id);
    }

    public PageResponse<MenuItem> items(int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) throw bad("page must be >= 0 and size must be 1..100");
        Comparator<MenuItem> comparator = switch (sort) {
            case "id,asc" -> Comparator.comparing(MenuItem::id);
            case "id,desc" -> Comparator.comparing(MenuItem::id).reversed();
            case "name,asc" -> Comparator.comparing(MenuItem::name).thenComparing(MenuItem::id);
            case "name,desc" -> Comparator.comparing(MenuItem::name).reversed().thenComparing(MenuItem::id);
            default -> throw bad("sort must be id,asc|id,desc|name,asc|name,desc");
        };
        List<MenuItem> all = catalog.items().stream().sorted(comparator).toList();
        long offset = (long) page * size;
        List<MenuItem> content = offset >= all.size() ? List.of() : all.subList((int) offset, (int) Math.min(offset + size, all.size()));
        return new PageResponse<>(content, page, size, all.size());
    }

    public List<MenuItem> menu(long sessionId) {
        Session session = activeSession(sessionId);
        return catalog.items().stream().filter(MenuItem::available)
                .filter(item -> item.packageIds().contains(session.packageId()))
                .sorted(Comparator.comparing(MenuItem::categoryId).thenComparing(MenuItem::name))
                .toList();
    }

    public Order place(long sessionId, PlaceOrderRequest request) {
        Session session = activeSession(sessionId);
        Set<Long> seen = new HashSet<>();
        List<OrderLine> lines = new ArrayList<>();
        for (OrderItemRequest requested : request.items()) {
            if (requested == null || requested.menuItemId() == null || requested.quantity() < 1) {
                throw bad("Each order item requires a menuItemId and quantity >= 1");
            }
            if (!seen.add(requested.menuItemId())) throw bad("Duplicate menuItemId");
            MenuItem item = item(requested.menuItemId());
            if (!item.available() || !item.packageIds().contains(session.packageId())) {
                throw bad("Menu item is unavailable for this package");
            }
            lines.add(new OrderLine(item.id(), item.name(), requested.quantity()));
        }
        return orders.save(new Order(null, sessionId, session.tableNumber(), List.copyOf(lines),
                OrderStatus.RECEIVED, OffsetDateTime.now(clock)));
    }

    public Order order(long id) { return orders.findOrder(id).orElseThrow(() -> missing("Order")); }
    public List<Order> orders(long sessionId) {
        if (sessions.find(sessionId).isEmpty()) throw missing("Session");
        return orders.forSession(sessionId);
    }

    private Session activeSession(long id) {
        Session session = sessions.find(id).orElseThrow(() -> missing("Session"));
        if (session.status() != DiningSessionStatus.ACTIVE) throw bad("Session is not active");
        return session;
    }

    private OrderingException missing(String entity) { return new OrderingException(HttpStatus.NOT_FOUND, entity + " not found"); }
    private OrderingException bad(String message) { return new OrderingException(HttpStatus.BAD_REQUEST, message); }
}
