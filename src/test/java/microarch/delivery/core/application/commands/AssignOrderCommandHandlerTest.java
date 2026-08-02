package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.services.OrderDispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class AssignOrderCommandHandlerTest {
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final OrderDispatchService orderDispatchService = mock(OrderDispatchService.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    @Test
    void AssignOrderCommandHandler_ShouldBeSuccess_WhenParamsAreCorrect() {
        // Arrange
        var orderId = UUID.randomUUID();
        var orderLocation = Location.create(1, 1).getValue();
        var volume = Volume.create(10).getValue();

        var order = Order.create(orderId, orderLocation, volume).getValue();

        var courierId = UUID.randomUUID();
        var name = "courier123";
        var courierLocation = Location.create(1, 1).getValue();

        var courier = Courier.create(courierId, name, courierLocation).getValue();
        var couriers = List.of(courier);

        when(orderRepository.findAnyCreated()).thenReturn(Optional.of(order));

        when(courierRepository.findAll()).thenReturn(List.of(courier));

        when(orderDispatchService.dispatch(any(), eq(order), eq(couriers))).thenReturn(Result.success(courier));

        // Act
        var handler = new AssignOrderCommandHandlerImpl(orderRepository, courierRepository, orderDispatchService,
                domainEventPublisher);

        var command = AssignOrderCommand.create().getValue();

        handler.handle(command);

        // Assert
        verify(orderRepository).update(order);
        verify(courierRepository).update(courier);
    }
}
