package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.CustomerOrder;
import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.exception.BusinessRuleException;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.mapper.OrderingMapper;
import com.buffetrestaurant.repository.CustomerOrderRepository;
import com.buffetrestaurant.service.OrderFulfillmentAccessProvider;
import com.buffetrestaurant.service.OrderFulfillmentService;
import com.buffetrestaurant.service.state.OrderState;
import com.buffetrestaurant.service.state.OrderStateResolver;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderFulfillmentServiceImpl implements OrderFulfillmentService {

    private final CustomerOrderRepository orderRepository;
    private final OrderingMapper mapper;
    private final OrderFulfillmentAccessProvider accessProvider;
    private final OrderStateResolver stateResolver;

    public OrderFulfillmentServiceImpl(
            CustomerOrderRepository orderRepository,
            OrderingMapper mapper,
            OrderFulfillmentAccessProvider accessProvider,
            OrderStateResolver stateResolver
    ) {
        this.orderRepository = orderRepository;
        this.mapper = mapper;
        this.accessProvider = accessProvider;
        this.stateResolver = stateResolver;
    }

    @Override
    public List<OrderResponse> getIncomingOrders() {
        accessProvider.requireKitchenAccess();
        return orderRepository.findByStatusInOrderByCreatedAtAsc(List.of(OrderStatus.RECEIVED, OrderStatus.PREPARING))
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<OrderResponse> getReadyOrders() {
        accessProvider.requireServiceStaffAccess();
        return orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.READY)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse advanceStatus(Long orderId, OrderStatus requestedStatus) {
        if (requestedStatus == OrderStatus.SERVED) {
            accessProvider.requireServiceStaffAccess();
        } else {
            accessProvider.requireKitchenAccess();
        }
        CustomerOrder order = findOrThrow(orderId);
        OrderState current = stateResolver.resolve(order.getStatus());
        OrderState next = current.next();
        if (next.status() != requestedStatus) {
            throw new BusinessRuleException("Cannot change order " + orderId + " status from " + order.getStatus()
                    + " to " + requestedStatus + "; the only allowed next status is " + next.status());
        }
        order.updateStatus(next.status());
        return mapper.toResponse(orderRepository.save(order));
    }

    private CustomerOrder findOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }
}
