package microarch.delivery.core.domain.model.assignment;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

import java.util.UUID;

@Getter
public final class Assignment extends BaseEntity<UUID> {
    private final UUID orderId;
    private final Volume volume;
    private final Location location;
    private AssignmentStatus status;

    private Assignment(UUID id, UUID orderId, Volume volume, Location location) {
        super(id);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = AssignmentStatus.ASSIGNED;
    }

    public static Result<Assignment, Error> create(UUID id, UUID orderId, Volume volume, Location location) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(id, "Id"), Guard.againstNullOrEmpty(orderId, "OrderId"),
                volume == null ? GeneralErrors.valueIsRequired("Volume") : null,
                location == null ? GeneralErrors.valueIsRequired("Location") : null);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Assignment(id, orderId, volume, location));
    }

    public UnitResult<Error> complete(Location courierLocation) {
        Error error = Guard.combine(
                courierLocation == null ? GeneralErrors.valueIsRequired("CourierLocation") : null,
                status == AssignmentStatus.COMPLETED ? AssignmentErrors.alreadyCompleted() : null,
                courierLocation != null && !location.isNeighbor(courierLocation)
                        ? AssignmentErrors.courierTooFarFromOrderLocation() : null);

        if (error != null) {
            return UnitResult.failure(error);
        }

        this.status = AssignmentStatus.COMPLETED;
        return UnitResult.success();
    }

}
