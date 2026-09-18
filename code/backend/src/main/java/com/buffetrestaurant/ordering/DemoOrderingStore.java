package com.buffetrestaurant.ordering;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.ordering.OrderingModels.*;
import com.buffetrestaurant.ordering.OrderingPorts.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

/** Temporary, process-local adapter. Replace these ports with persistence/session adapters at integration. */
@Repository
@Profile("!persistence")
public class DemoOrderingStore implements Catalog, Sessions, Orders {
    private final Map<Long, Category> categories = new ConcurrentHashMap<>();
    private final Map<Long, MenuItem> items = new ConcurrentHashMap<>();
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong categoryIds = new AtomicLong();
    private final AtomicLong itemIds = new AtomicLong();
    private final AtomicLong orderIds = new AtomicLong();

    public DemoOrderingStore() {
        Category main = saveCategory(null, "อาหารหลัก");
        Category drinks = saveCategory(null, "เครื่องดื่ม");
        saveItem(new MenuItem(null, main.id(), "หมูสไลซ์", true, Set.of(1L), null));
        saveItem(new MenuItem(null, main.id(), "ผักรวม", true, Set.of(1L), null));
        saveItem(new MenuItem(null, drinks.id(), "ชาเย็น", true, Set.of(1L), null));
    }

    public List<Category> categories() { return categories.values().stream().sorted(Comparator.comparing(Category::id)).toList(); }
    public Optional<Category> category(long id) { return Optional.ofNullable(categories.get(id)); }
    public Category saveCategory(Long id, String name) {
        long key = id == null ? categoryIds.incrementAndGet() : id;
        Category value = new Category(key, name);
        categories.put(key, value);
        return value;
    }
    public void deleteCategory(long id) { categories.remove(id); }
    public List<MenuItem> items() { return new ArrayList<>(items.values()); }
    public Optional<MenuItem> item(long id) { return Optional.ofNullable(items.get(id)); }
    public MenuItem saveItem(MenuItem item) {
        long key = item.id() == null ? itemIds.incrementAndGet() : item.id();
        MenuItem value = new MenuItem(key, item.categoryId(), item.name(), item.available(), Set.copyOf(item.packageIds()), item.imageUrl());
        items.put(key, value);
        return value;
    }
    public void deleteItem(long id) { items.remove(id); }
    public Optional<Session> find(long id) {
        return id == 1 ? Optional.of(new Session(1L, 1L, "A01", DiningSessionStatus.ACTIVE)) : Optional.empty();
    }
    public Order save(Order order) {
        long key = order.orderId() == null ? orderIds.incrementAndGet() : order.orderId();
        Order value = new Order(key, order.sessionId(), order.tableNumber(), List.copyOf(order.items()), order.status(), order.createdAt());
        orders.put(key, value);
        return value;
    }
    public Optional<Order> findOrder(long id) { return Optional.ofNullable(orders.get(id)); }
    public List<Order> forSession(long sessionId) {
        return orders.values().stream().filter(order -> order.sessionId() == sessionId)
                .sorted(Comparator.comparing(Order::createdAt).thenComparing(Order::orderId)).toList();
    }
}
