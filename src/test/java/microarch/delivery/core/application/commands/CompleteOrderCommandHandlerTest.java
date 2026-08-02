package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class CompleteOrderCommandHandlerTest {
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final Courier courier = mock(Courier.class);
    private final Order order = mock(Order.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    @Test
    void CompleteOrderCommandHandler_ShouldBeSuccess_WhenParamsAreCorrect() {
        // Arrange
        var orderId = UUID.randomUUID();
        var courierId = UUID.randomUUID();

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(courier.completeAssignment(orderId)).thenReturn(UnitResult.success());

        when(order.complete()).thenReturn(UnitResult.success());

        // Act
        var handler = new CompleteOrderCommandHandlerImpl(courierRepository, orderRepository, domainEventPublisher);

        var command = CompleteOrderCommand.create(courierId, orderId).getValue();

        handler.handle(command);

        // Assert
        verify(courierRepository).findById(courierId);
        verify(orderRepository).findById(orderId);
        verify(courier).completeAssignment(orderId);
        verify(order).complete();
        verify(courierRepository).update(courier);
        verify(orderRepository).update(order);
    }
}
