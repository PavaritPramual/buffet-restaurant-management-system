package com.buffetrestaurant.ordering;

import com.buffetrestaurant.ordering.OrderingModels.Category;
import com.buffetrestaurant.ordering.OrderingModels.MenuItem;
import com.buffetrestaurant.ordering.OrderingModels.Order;
import com.buffetrestaurant.ordering.OrderingModels.Session;
import java.util.List;
import java.util.Optional;

public final class OrderingPorts {
    private OrderingPorts() {}

    public interface Catalog {
        List<Category> categories();
        Optional<Category> category(long id);
        Category saveCategory(Long id, String name);
        void deleteCategory(long id);
        List<MenuItem> items();
        Optional<MenuItem> item(long id);
        MenuItem saveItem(MenuItem item);
        void deleteItem(long id);
    }

    public interface Sessions {
        Optional<Session> find(long id);
    }

    public interface Orders {
        Order save(Order order);
        Optional<Order> findOrder(long id);
        List<Order> forSession(long sessionId);
    }
}
