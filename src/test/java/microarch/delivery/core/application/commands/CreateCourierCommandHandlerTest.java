package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class CreateCourierCommandHandlerTest {
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    @Test
    void CreateCourierCommandHandler_ShouldBeSuccess_WhenParamsAreCorrect() {
        // Arrange
        var courierId = UUID.randomUUID();
        var name = "courier123";

        // Act
        var handler = new CreateCourierCommandHandlerImpl(courierRepository, domainEventPublisher);

        var command = CreateCourierCommand.create(
                courierId,
                name
        ).getValue();

        handler.handle(command);

        // Assert
        ArgumentCaptor<Courier> captor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository).save(captor.capture());

        assertThat(captor.getAllValues().size()).isEqualTo(1);

        var courier = captor.getValue();
        assertThat(courier.getId()).isEqualTo(courierId);
        assertThat(courier.getName()).isEqualTo(name);
    }
}
