package com.buffetrestaurant.ordering;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.ordering.OrderingModels.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(ApiPaths.API_V1)
public class OrderingController {
    private final OrderingService service;

    public OrderingController(OrderingService service) { this.service = service; }

    @GetMapping("/menu-categories")
    public List<Category> categories() { return service.categories(); }

    @GetMapping("/menu-categories/{id}")
    public Category category(@PathVariable @Positive long id) { return service.category(id); }

    @PostMapping("/menu-categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category result = service.saveCategory(null, request.name());
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/menu-categories/" + result.id())).body(result);
    }

    @PutMapping("/menu-categories/{id}")
    public Category updateCategory(@PathVariable @Positive long id, @Valid @RequestBody CategoryRequest request) {
        return service.saveCategory(id, request.name());
    }

    @DeleteMapping("/menu-categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable @Positive long id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/menu-items")
    public PageResponse<MenuItem> items(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "id,asc") String sort) {
        return service.items(page, size, sort);
    }

    @GetMapping("/menu-items/{id}")
    public MenuItem item(@PathVariable @Positive long id) { return service.item(id); }

    @PostMapping("/menu-items")
    public ResponseEntity<MenuItem> createItem(@Valid @RequestBody MenuItemRequest request) {
        MenuItem result = service.saveItem(null, request);
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/menu-items/" + result.id())).body(result);
    }

    @PutMapping("/menu-items/{id}")
    public MenuItem updateItem(@PathVariable @Positive long id, @Valid @RequestBody MenuItemRequest request) {
        return service.saveItem(id, request);
    }

    @DeleteMapping("/menu-items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable @Positive long id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dining-sessions/{id}/menu")
    public List<MenuItem> menu(@PathVariable @Positive long id) { return service.menu(id); }

    @PostMapping("/dining-sessions/{id}/orders")
    public ResponseEntity<Order> place(@PathVariable @Positive long id, @Valid @RequestBody PlaceOrderRequest request) {
        Order result = service.place(id, request);
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/orders/" + result.orderId())).body(result);
    }

    @GetMapping("/dining-sessions/{id}/orders")
    public List<Order> orders(@PathVariable @Positive long id) { return service.orders(id); }

    @GetMapping("/orders/{id}")
    public Order order(@PathVariable @Positive long id) { return service.order(id); }
}
