package microarch.delivery.adapters.in.http;

import jakarta.annotation.Generated;
import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetOrdersApi;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQuery;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-18T18:31:02.352874400+03:00[Europe/Moscow]", comments = "Generator version: 7.23.0")
@RestController
@RequestMapping("${openapi.swaggerDelivery.base-path:}")
@RequiredArgsConstructor
public class GetOrdersApiController implements GetOrdersApi {
    private final GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler;

    @Override
    public ResponseEntity<List<Order>> getOrders() {
        // Формируем команду
        var createCommandResult = GetNotCompletedOrdersQuery.create();
        if (createCommandResult.isFailure())
            return ResponseEntity.badRequest().build();
        var command = createCommandResult.getValue();

        // Обрабатываем команду
        var handleCommandResult = this.getNotCompletedOrdersQueryHandler.handle(command);
        if (handleCommandResult.isFailure())
            return ResponseEntity.badRequest().build();

        // Формируем ответ
        var response = handleCommandResult
                .getValue()
                .stream()
                .map(a -> new Order(
                        a.orderId(),
                        new Location(a.location().x(), a.location().y()))
                )
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
