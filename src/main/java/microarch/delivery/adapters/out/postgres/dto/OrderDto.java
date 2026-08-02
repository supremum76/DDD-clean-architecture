package microarch.delivery.adapters.out.postgres.dto;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import java.util.UUID;

public record OrderDto(UUID id, int status, int volume, int locationX, int locationY) {

    public Order toDomain() {
        return Order.dto2domain(this.id, Location.create(this.locationX, this.locationY).getValueOrThrow(),
                Volume.create(this.volume).getValueOrThrow(), OrderStatus.fromCode(this.status));
    }
}
