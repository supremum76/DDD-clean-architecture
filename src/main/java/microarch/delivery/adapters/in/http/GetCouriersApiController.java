package microarch.delivery.adapters.in.http;

import jakarta.annotation.Generated;
import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetCouriersApi;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.queries.GetAllCouriersQuery;
import microarch.delivery.core.application.queries.GetAllCouriersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-18T18:31:02.352874400+03:00[Europe/Moscow]", comments = "Generator version: 7.23.0")
@RestController
@RequestMapping("${openapi.swaggerDelivery.base-path:}")
@RequiredArgsConstructor
public class GetCouriersApiController implements GetCouriersApi {
    private final GetAllCouriersQueryHandler getAllCouriersQueryHandler;

    @Override
    public ResponseEntity<List<Courier>> getCouriers() {
        // Формируем команду
        var createCommandResult = GetAllCouriersQuery.create();
        if (createCommandResult.isFailure())
            return ResponseEntity.badRequest().build();
        var command = createCommandResult.getValue();

        // Обрабатываем команду
        var handleCommandResult = this.getAllCouriersQueryHandler.handle(command);
        if (handleCommandResult.isFailure())
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // Формируем ответ
        var response = handleCommandResult
                .getValue()
                .stream()
                .map(a -> new Courier(
                        a.courierId(),
                        a.name(),
                        new Location(a.location().x(), a.location().y()))
                )
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
