package microarch.delivery.core.application.queries;

import microarch.delivery.core.application.queries.dto.LocationDto;

import java.util.UUID;

public record GetNotCompletedOrdersResponse(UUID orderId, LocationDto location) {
}
