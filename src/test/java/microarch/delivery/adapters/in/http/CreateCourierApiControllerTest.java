package microarch.delivery.adapters.in.http;

import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.adapters.in.http.model.CreateCourierResponse;
import microarch.delivery.adapters.in.http.model.NewCourier;
import microarch.delivery.core.application.commands.CreateCourierCommand;
import microarch.delivery.core.application.commands.CreateCourierCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateCourierApiControllerTest {

    private CreateCourierCommandHandler createCourierHandler;
    private CreateCourierApiController controller;

    @BeforeEach
    void setUp() {
        createCourierHandler = mock(CreateCourierCommandHandler.class);
        controller = new CreateCourierApiController(createCourierHandler);
    }

    @Test
    void createCourier_ShouldReturnOk_WhenHandlerSuccess() {
        // Arrange
        var newCourier = new NewCourier("Courier 123");

        // Мокаем успешную обработку команды
        when(createCourierHandler.handle(any(CreateCourierCommand.class))).thenReturn(UnitResult.success());

        // Act
        ResponseEntity<CreateCourierResponse> response = controller.createCourier(newCourier);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(createCourierHandler, times(1)).handle(any(CreateCourierCommand.class));
    }

    @Test
    void createCourier_ShouldReturnConflict_WhenHandlerFails() {
        // Arrange
        var newCourier = new NewCourier("Courier 123");

        // Мокаем неуспешную обработку команды
        when(createCourierHandler.handle(any(CreateCourierCommand.class))).thenReturn(
                UnitResult.failure(GeneralErrors.valueIsInvalid("courier", newCourier)));

        // Act
        ResponseEntity<CreateCourierResponse> response = controller.createCourier(newCourier);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(createCourierHandler, times(1)).handle(any(CreateCourierCommand.class));
    }

    @Test
    void createCourier_ShouldReturnBadRequest_WhenCommandCreationFails() {
        // Arrange
        var newCourier = new NewCourier(""); // пустая строка имени курьера

        // Act
        ResponseEntity<CreateCourierResponse> response = controller.createCourier(newCourier);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(createCourierHandler);
    }
}