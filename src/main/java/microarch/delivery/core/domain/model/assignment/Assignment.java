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
import microarch.delivery.core.domain.model.courier.Courier;

import java.util.List;
import java.util.UUID;

@Getter
public final class Assignment extends BaseEntity {
    private final UUID orderId;
    private final Volume volume;
    private final Location location;
    private AssignmentStatus status;

    private Assignment(UUID id, UUID orderId, Volume volume, Location location, AssignmentStatus status) {
        super(id);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = status;
    }

    public static Result<Assignment, Error> create(UUID id, UUID orderId, Volume volume, Location location) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(id, "Id"), Guard.againstNullOrEmpty(orderId, "OrderId"),
                volume == null ? GeneralErrors.valueIsRequired("Volume") : null,
                location == null ? GeneralErrors.valueIsRequired("Location") : null);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Assignment(id, orderId, volume, location, AssignmentStatus.ASSIGNED));
    }

    static public Assignment dto2domain(UUID id, UUID orderId, Volume volume, Location location,
            AssignmentStatus status) {
        return new Assignment(id, orderId, volume, location, status);
    }

    public UnitResult<Error> complete(Location courierLocation) {
        Error error = Guard.combine(courierLocation == null ? GeneralErrors.valueIsRequired("CourierLocation") : null,
                status == AssignmentStatus.COMPLETED ? AssignmentErrors.alreadyCompleted() : null,
                courierLocation != null && !location.isNeighbor(courierLocation)
                        ? AssignmentErrors.courierTooFarFromOrderLocation() : null);

        if (error != null) {
            return UnitResult.failure(error);
        }

        this.status = AssignmentStatus.COMPLETED;
        return UnitResult.success();
    }

    public boolean canBeCompleted(Location courierLocation) {
        return status == AssignmentStatus.ASSIGNED && location.isNeighbor(courierLocation);
    }

}
