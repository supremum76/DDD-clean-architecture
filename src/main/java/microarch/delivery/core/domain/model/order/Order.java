package microarch.delivery.core.domain.model.order;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;

import java.util.UUID;

@Getter
public final class Order extends Aggregate<UUID> {
    private final Location location;
    private final Volume volume;
    private OrderStatus status;

    private Order(UUID id, Location location, Volume volume, OrderStatus status) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.status = status;
    }

    public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(id, "Id"),
                location == null ? GeneralErrors.valueIsRequired("Location") : null,
                volume == null ? GeneralErrors.valueIsRequired("Volume") : null);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Order(id, location, volume, OrderStatus.CREATED));
    }

    public static Order dto2domain(UUID id, Location location, Volume volume, OrderStatus status) {
        return new Order(id, location, volume, status);
    }

    public UnitResult<Error> assign() {
        if (status != OrderStatus.CREATED) {
            return UnitResult.failure(OrderErrors.invalidStatusForAssign());
        }

        this.status = OrderStatus.ASSIGNED;
        // Порождаем доменное событие
        raiseDomainEvent(new OrderAssignedDomainEvent(this.id));
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        if (status != OrderStatus.ASSIGNED) {
            return UnitResult.failure(OrderErrors.invalidStatusForComplete());
        }

        this.status = OrderStatus.COMPLETED;
        return UnitResult.success();
    }
}
