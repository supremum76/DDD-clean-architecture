package microarch.delivery.adapters.in.http;

import jakarta.annotation.Generated;
import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateCourierApi;
import microarch.delivery.adapters.in.http.model.CreateCourierResponse;
import microarch.delivery.adapters.in.http.model.NewCourier;
import microarch.delivery.core.application.commands.CreateCourierCommand;
import microarch.delivery.core.application.commands.CreateCourierCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-18T18:31:02.352874400+03:00[Europe/Moscow]", comments = "Generator version: 7.23.0")
@RestController
@RequestMapping("${openapi.swaggerDelivery.base-path:}")
@RequiredArgsConstructor
public class CreateCourierApiController implements CreateCourierApi {
    private final CreateCourierCommandHandler createCourierCommandHandler;

    @Override
    public ResponseEntity<CreateCourierResponse> createCourier(NewCourier newCourier) {
        final var courierId = UUID.randomUUID();

        // Формируем команду
        var createCommandResult = CreateCourierCommand.create(courierId, newCourier.getName());
        if (createCommandResult.isFailure())
            return ResponseEntity.badRequest().build();
        var command = createCommandResult.getValue();

        // Обрабатываем команду
        var handleCommandResult = this.createCourierCommandHandler.handle(command);
        if (handleCommandResult.isFailure())
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // Формируем ответ
        var response = new CreateCourierResponse(courierId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
