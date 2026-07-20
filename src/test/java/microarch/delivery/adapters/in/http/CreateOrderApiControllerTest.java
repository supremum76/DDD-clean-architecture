package microarch.delivery.adapters.in.http;

import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.adapters.in.http.model.Address;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import microarch.delivery.adapters.in.http.model.NewOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateOrderApiControllerTest {

    private CreateOrderCommandHandler createOrderHandler;
    private CreateOrderApiController controller;

    @BeforeEach
    void setUp() {
        createOrderHandler = mock(CreateOrderCommandHandler.class);
        controller = new CreateOrderApiController(createOrderHandler);
    }

    @Test
    void createOrder_ShouldReturnOk_WhenHandlerSuccess() {
        // Arrange
        var orderId = UUID.randomUUID();
        var address = new Address().country("RU").city("Moscow").street("Tverskaya").house("1").apartment("2");
        var volume = 10;

        var newOrder = new NewOrder(orderId, address, volume);

        // Мокаем успешную обработку команды
        when(createOrderHandler.handle(any(CreateOrderCommand.class))).thenReturn(UnitResult.success());

        // Act
        ResponseEntity<CreateOrderResponse> response = controller.createOrder(newOrder);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(createOrderHandler, times(1)).handle(any(CreateOrderCommand.class));
    }

    @Test
    void createOrder_ShouldReturnConflict_WhenHandlerFails() {
        // Arrange
        var orderId = UUID.randomUUID();
        var address = new Address().country("RU").city("Moscow").street("Tverskaya").house("1").apartment("2");
        var volume = 10;

        var newOrder = new NewOrder(orderId, address, volume);

        // Мокаем неуспешную обработку команды
        when(createOrderHandler.handle(any(CreateOrderCommand.class))).thenReturn(
                UnitResult.failure(GeneralErrors.valueIsInvalid("order", newOrder)));

        // Act
        ResponseEntity<CreateOrderResponse> response = controller.createOrder(newOrder);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(createOrderHandler, times(1)).handle(any(CreateOrderCommand.class));
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenCommandCreationFails() {
        // Arrange
        var orderId = UUID.randomUUID();
        var address = new Address() // Некорректные данные, например пустая страна
                .country("").city("Moscow").street("Tverskaya").house("1").apartment("2");
        var volume = 10;

        var newOrder = new NewOrder(orderId, address, volume);

        // Act
        ResponseEntity<CreateOrderResponse> response = controller.createOrder(newOrder);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(createOrderHandler);
    }
}

