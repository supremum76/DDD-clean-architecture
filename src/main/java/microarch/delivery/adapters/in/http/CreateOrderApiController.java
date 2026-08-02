package microarch.delivery.adapters.in.http;

import jakarta.annotation.Generated;
import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateOrderApi;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-18T18:31:02.352874400+03:00[Europe/Moscow]", comments = "Generator version: 7.23.0")
@RestController
@RequestMapping("${openapi.swaggerDelivery.base-path:}")
@RequiredArgsConstructor
public class CreateOrderApiController implements CreateOrderApi {
    private final CreateOrderCommandHandler createOrderCommandHandler;

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(NewOrder newOrder) {
        // Формируем команду
        var address = newOrder.getAddress();
        var createCommandResult = CreateOrderCommand.create(newOrder.getId(),

                newOrder.getVolume(),

                address.getCountry(), address.getCity(), address.getStreet(), address.getHouse(),
                address.getApartment());
        if (createCommandResult.isFailure())
            return ResponseEntity.badRequest().build();
        var command = createCommandResult.getValue();

        // Обрабатываем команду
        var handleCommandResult = this.createOrderCommandHandler.handle(command);
        if (handleCommandResult.isFailure())
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // Формируем ответ
        var response = new CreateOrderResponse(newOrder.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
