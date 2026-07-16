package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class CompleteOrderCommandHandlerTest {
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final Courier courier = mock(Courier.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    @Test
    void CompleteOrderCommandHandler_ShouldBeSuccess_WhenParamsAreCorrect() {
        // Arrange
        var assignmentId = UUID.randomUUID();
        var courierId = UUID.randomUUID();

        when(courierRepository.findById(courierId))
                .thenReturn(Optional.of(courier));

        when(courier.completeAssignment(assignmentId))
                .thenReturn(UnitResult.success());

        // Act
        var handler = new CompleteOrderCommandHandlerImpl(
                courierRepository,
                domainEventPublisher
        );

        var command = CompleteOrderCommand.create(courierId, assignmentId).getValue();

        handler.handle(command);

        // Assert
        verify(courierRepository).findById(courierId);
        verify(courier).completeAssignment(assignmentId);
        verify(courierRepository).update(courier);
    }
}
