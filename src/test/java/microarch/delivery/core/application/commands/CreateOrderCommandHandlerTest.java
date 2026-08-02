package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CreateOrderCommandHandlerTest {
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    @Test
    void CreateOrderCommandHandler_ShouldBeSuccess_WhenParamsAreCorrect() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        int volume = 10;

        String country = "country123";
        String city = "city123";
        String street = "street123";
        String house = "house123";
        String apartment = "apartment123";

        // Act
        var handler = new CreateOrderCommandHandlerImpl(orderRepository, domainEventPublisher);

        var command = CreateOrderCommand.create(orderId, volume, country, city, street, house, apartment).getValue();

        handler.handle(command);

        // Assert
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        assertThat(captor.getAllValues().size()).isEqualTo(1);

        var order = captor.getValue();
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getVolume().getValue()).isEqualTo(volume);
    }
}
