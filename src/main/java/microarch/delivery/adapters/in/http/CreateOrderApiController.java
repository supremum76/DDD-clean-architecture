package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateOrderApi;
import microarch.delivery.adapters.in.http.model.CreateCourierResponse;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.adapters.in.http.model.Error;
import microarch.delivery.adapters.in.http.model.NewOrder;


import microarch.delivery.core.application.commands.CreateCourierCommand;
import microarch.delivery.core.application.commands.CreateCourierCommandHandler;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-18T18:31:02.352874400+03:00[Europe/Moscow]", comments = "Generator version: 7.23.0")
@RestController
@RequestMapping("${openapi.swaggerDelivery.base-path:}")
@RequiredArgsConstructor
public class CreateOrderApiController implements CreateOrderApi {
    private final CreateOrderCommandHandler createOrderCommandHandler;

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(NewOrder newOrder) {
        final var orderId = UUID.randomUUID();

        // Формируем команду
        final var address = newOrder.getAddress();
        var createCommandResult = CreateOrderCommand.create(
                orderId,

                newOrder.getVolume(),

                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getApartment()
        );
        if (createCommandResult.isFailure())
            return ResponseEntity.badRequest().build();
        var command = createCommandResult.getValue();

        // Обрабатываем команду
        var handleCommandResult = this.createOrderCommandHandler.handle(command);
        if (handleCommandResult.isFailure())
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // Формируем ответ
        var response = new CreateOrderResponse(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
