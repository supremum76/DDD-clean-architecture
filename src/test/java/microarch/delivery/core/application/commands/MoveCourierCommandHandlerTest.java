package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class MoveCourierCommandHandlerTest {
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final Courier courier = mock(Courier.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    @Test
    void MoveCourierCommandHandler_ShouldBeSuccess_WhenParamsAreCorrect() {
        // Arrange
        var courierId = UUID.randomUUID();
        var location = Location.create(1, 1).getValue();

        when(courierRepository.findById(courierId))
                .thenReturn(Optional.of(courier));

        when(courier.move(location))
                .thenReturn(UnitResult.success());

        // Act
        var handler = new MoveCourierCommandHandlerImpl(
                courierRepository,
                domainEventPublisher
        );

        var command = MoveCourierCommand.create(courierId, location.getX(), location.getY()).getValue();

        handler.handle(command);

        // Assert
        verify(courierRepository).findById(courierId);
        verify(courier).move(location);
        verify(courierRepository).update(courier);
    }
}
