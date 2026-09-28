package com.buffetrestaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buffetrestaurant.domain.CustomerOrder;
import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.exception.BusinessRuleException;
import com.buffetrestaurant.exception.ForbiddenException;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.mapper.OrderingMapper;
import com.buffetrestaurant.repository.CustomerOrderRepository;
import com.buffetrestaurant.service.impl.OrderFulfillmentServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderFulfillmentServiceTest {

    @Mock
    private CustomerOrderRepository orderRepository;

    @Mock
    private OrderFulfillmentAccessProvider accessProvider;

    private OrderFulfillmentService fulfillmentService;

    @BeforeEach
    void setUp() {
        fulfillmentService = new OrderFulfillmentServiceImpl(orderRepository, new OrderingMapper(), accessProvider);
    }

    private CustomerOrder orderWithStatus(Long id, OrderStatus status) {
        CustomerOrder order = new CustomerOrder(1L, "A01");
        order.updateStatus(status);
        setId(order, id);
        return order;
    }

    private void setId(CustomerOrder order, Long id) {
        try {
            var field = CustomerOrder.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(order, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void getIncomingOrders_whenCalled_returnsReceivedAndPreparingOrders() {
        CustomerOrder received = orderWithStatus(1L, OrderStatus.RECEIVED);
        CustomerOrder preparing = orderWithStatus(2L, OrderStatus.PREPARING);
        when(orderRepository.findByStatusInOrderByCreatedAtAsc(List.of(OrderStatus.RECEIVED, OrderStatus.PREPARING)))
                .thenReturn(List.of(received, preparing));

        List<OrderResponse> result = fulfillmentService.getIncomingOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).status()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(result.get(1).status()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void getReadyOrders_whenCalled_returnsOnlyReadyOrders() {
        CustomerOrder ready = orderWithStatus(3L, OrderStatus.READY);
        when(orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.READY)).thenReturn(List.of(ready));

        List<OrderResponse> result = fulfillmentService.getReadyOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void advanceStatus_whenReceivedToPreparing_updatesAndReturnsOrder() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.RECEIVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = fulfillmentService.advanceStatus(1L, OrderStatus.PREPARING);

        assertThat(result.status()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void advanceStatus_whenPreparingToReady_updatesAndReturnsOrder() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.PREPARING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = fulfillmentService.advanceStatus(1L, OrderStatus.READY);

        assertThat(result.status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void advanceStatus_whenReadyToServed_updatesAndReturnsOrder() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = fulfillmentService.advanceStatus(1L, OrderStatus.SERVED);

        assertThat(result.status()).isEqualTo(OrderStatus.SERVED);
    }

    @Test
    void advanceStatus_whenSkippingReceivedToReady_rejectsWithBusinessRuleException() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.RECEIVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> fulfillmentService.advanceStatus(1L, OrderStatus.READY))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("RECEIVED")
                .hasMessageContaining("READY");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void advanceStatus_whenSkippingReceivedToServed_rejectsWithBusinessRuleException() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.RECEIVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> fulfillmentService.advanceStatus(1L, OrderStatus.SERVED))
                .isInstanceOf(BusinessRuleException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void advanceStatus_whenReversingPreparingToReceived_rejectsWithBusinessRuleException() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.PREPARING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> fulfillmentService.advanceStatus(1L, OrderStatus.RECEIVED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PREPARING")
                .hasMessageContaining("RECEIVED");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void advanceStatus_whenReversingReadyToPreparing_rejectsWithBusinessRuleException() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> fulfillmentService.advanceStatus(1L, OrderStatus.PREPARING))
                .isInstanceOf(BusinessRuleException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void advanceStatus_whenOrderAlreadyServed_rejectsWithBusinessRuleException() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.SERVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> fulfillmentService.advanceStatus(1L, OrderStatus.SERVED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already been served");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void advanceStatus_whenOrderNotFound_throwsResourceNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fulfillmentService.advanceStatus(999L, OrderStatus.PREPARING))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getIncomingOrders_whenCalled_requiresKitchenAccess() {
        when(orderRepository.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(List.of());

        fulfillmentService.getIncomingOrders();

        verify(accessProvider).requireKitchenAccess();
    }

    @Test
    void getIncomingOrders_whenAccessDenied_propagatesAndNeverQueriesRepository() {
        doThrow(new ForbiddenException("Role SERVICE_STAFF is not permitted to perform this action"))
                .when(accessProvider).requireKitchenAccess();

        assertThatThrownBy(() -> fulfillmentService.getIncomingOrders()).isInstanceOf(ForbiddenException.class);
        verify(orderRepository, never()).findByStatusInOrderByCreatedAtAsc(any());
    }

    @Test
    void getReadyOrders_whenCalled_requiresServiceStaffAccess() {
        when(orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.READY)).thenReturn(List.of());

        fulfillmentService.getReadyOrders();

        verify(accessProvider).requireServiceStaffAccess();
    }

    @Test
    void getReadyOrders_whenAccessDenied_propagatesAndNeverQueriesRepository() {
        doThrow(new ForbiddenException("Role KITCHEN_STAFF is not permitted to perform this action"))
                .when(accessProvider).requireServiceStaffAccess();

        assertThatThrownBy(() -> fulfillmentService.getReadyOrders()).isInstanceOf(ForbiddenException.class);
        verify(orderRepository, never()).findByStatusOrderByCreatedAtAsc(any());
    }

    @Test
    void advanceStatus_whenTargetIsPreparing_requiresKitchenAccessNotServiceStaffAccess() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.RECEIVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fulfillmentService.advanceStatus(1L, OrderStatus.PREPARING);

        verify(accessProvider).requireKitchenAccess();
        verify(accessProvider, never()).requireServiceStaffAccess();
    }

    @Test
    void advanceStatus_whenTargetIsReady_requiresKitchenAccessNotServiceStaffAccess() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.PREPARING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fulfillmentService.advanceStatus(1L, OrderStatus.READY);

        verify(accessProvider).requireKitchenAccess();
        verify(accessProvider, never()).requireServiceStaffAccess();
    }

    @Test
    void advanceStatus_whenTargetIsServed_requiresServiceStaffAccessNotKitchenAccess() {
        CustomerOrder order = orderWithStatus(1L, OrderStatus.READY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fulfillmentService.advanceStatus(1L, OrderStatus.SERVED);

        verify(accessProvider).requireServiceStaffAccess();
        verify(accessProvider, never()).requireKitchenAccess();
    }

    @Test
    void advanceStatus_whenKitchenAccessDenied_neverLooksUpOrder() {
        doThrow(new ForbiddenException("Role SERVICE_STAFF is not permitted to perform this action"))
                .when(accessProvider).requireKitchenAccess();

        assertThatThrownBy(() -> fulfillmentService.advanceStatus(1L, OrderStatus.PREPARING))
                .isInstanceOf(ForbiddenException.class);
        verify(orderRepository, never()).findById(any());
    }

    @Test
    void advanceStatus_whenServiceStaffAccessDenied_neverLooksUpOrder() {
        doThrow(new ForbiddenException("Role KITCHEN_STAFF is not permitted to perform this action"))
                .when(accessProvider).requireServiceStaffAccess();

        assertThatThrownBy(() -> fulfillmentService.advanceStatus(1L, OrderStatus.SERVED))
                .isInstanceOf(ForbiddenException.class);
        verify(orderRepository, never()).findById(any());
    }
}
