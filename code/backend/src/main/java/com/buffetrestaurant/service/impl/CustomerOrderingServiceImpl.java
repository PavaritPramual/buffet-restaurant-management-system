package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.CustomerOrder;
import com.buffetrestaurant.domain.MenuItem;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.dto.request.OrderItemRequest;
import com.buffetrestaurant.dto.request.PlaceOrderRequest;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.exception.BusinessRuleException;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.mapper.OrderingMapper;
import com.buffetrestaurant.repository.CustomerOrderRepository;
import com.buffetrestaurant.repository.MenuItemRepository;
import com.buffetrestaurant.service.CustomerOrderingService;
import com.buffetrestaurant.service.SessionContextProvider;
import com.buffetrestaurant.service.SessionContextProvider.SessionContextSnapshot;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerOrderingServiceImpl implements CustomerOrderingService {
    private final SessionContextProvider sessionProvider;
    private final MenuItemRepository menuItemRepository;
    private final CustomerOrderRepository orderRepository;
    private final OrderingMapper mapper;

    public CustomerOrderingServiceImpl(SessionContextProvider sessionProvider, MenuItemRepository menuItemRepository,
                                       CustomerOrderRepository orderRepository, OrderingMapper mapper) {
        this.sessionProvider = sessionProvider;
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    public List<MenuItemResponse> getMenu(Long sessionId) {
        SessionContextSnapshot session = requireActive(sessionId);
        return menuItemRepository.findAvailableForPackage(session.packageId()).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public OrderResponse placeOrder(Long sessionId, PlaceOrderRequest request) {
        SessionContextSnapshot session = requireActive(sessionId);
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemRequest requested : request.items()) {
            if (quantities.putIfAbsent(requested.menuItemId(), requested.quantity()) != null) {
                throw new BusinessRuleException("Duplicate menu item in order: " + requested.menuItemId());
            }
        }
        CustomerOrder order = new CustomerOrder(sessionId, session.tableNumber());
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            MenuItem item = menuItemRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + entry.getKey()));
            if (!item.isAvailable()) throw new BusinessRuleException("Menu item is unavailable: " + item.getName());
            if (!item.getPackageIds().contains(session.packageId())) throw new BusinessRuleException("Menu item is not included in this package: " + item.getName());
            order.addItem(item.getId(), item.getName(), entry.getValue());
        }
        return mapper.toResponse(orderRepository.save(order));
    }

    public List<OrderResponse> getOrders(Long sessionId) {
        sessionProvider.requireSession(sessionId);
        return orderRepository.findBySessionIdOrderByCreatedAtDesc(sessionId).stream().map(mapper::toResponse).toList();
    }

    public OrderResponse getOrder(Long orderId) {
        return mapper.toResponse(orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId)));
    }

    private SessionContextSnapshot requireActive(Long sessionId) {
        SessionContextSnapshot session = sessionProvider.requireSession(sessionId);
        if (session.status() != DiningSessionStatus.ACTIVE) throw new BusinessRuleException("Dining session is not active");
        return session;
    }
}
