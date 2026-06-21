package microarch.delivery.core.domain.model.courier;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.Assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public final class Courier extends Aggregate<UUID> {
    private static final int MAX_VOLUME = 20;

    private final String name;
    private Location location;
    private final List<Assignment> assignments;

    private Courier(UUID id, String name, Location location) {
        super(id);
        this.name = name;
        this.location = location;
        this.assignments = new ArrayList<>();
    }

    public static Result<Courier, Error> create(UUID id, String name, Location location) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(id, "Id"), Guard.againstNullOrEmpty(name, "Name"),
                location == null ? GeneralErrors.valueIsRequired("Location") : null);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Courier(id, name, location));
    }

    public boolean canBeAssigned(Volume volume) {
        return getCurrentVolume() + volume.getValue() <= MAX_VOLUME;
    }

    public UnitResult<Error> takeOrder(UUID assignmentId, UUID orderId, Volume volume, Location orderLocation) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(assignmentId, "AssignmentId"),
                Guard.againstNullOrEmpty(orderId, "OrderId"),
                volume == null ? GeneralErrors.valueIsRequired("Volume") : null,
                orderLocation == null ? GeneralErrors.valueIsRequired("OrderLocation") : null,
                volume != null && !canBeAssigned(volume) ? CourierErrors.cannotTakeOrder() : null);

        if (error != null) {
            return UnitResult.failure(error);
        }

        Result<Assignment, Error> assignment = Assignment.create(assignmentId, orderId, volume, orderLocation);
        if(assignment.isFailure()) {
            return UnitResult.failure(assignment.getError());    
        }
        assignments.add(assignment.getValue());

        return UnitResult.success();
    }

    public UnitResult<Error> completeAssignment(UUID assignmentId) {
        Assignment assignment = findAssignment(assignmentId);

        if (assignment == null) {
            return UnitResult.failure(CourierErrors.assignmentNotFound());
        }

        if (!assignment.canBeCompleted(location)) {
            return UnitResult.failure(CourierErrors.assignmentCannotBeCompleted());
        }

        UnitResult<Error> completeResult = assignment.complete(location);
        if (completeResult.isFailure()) {
            return UnitResult.failure(completeResult.getError());
        }
        assignments.remove(assignment);

        return UnitResult.success();
    }

    public UnitResult<Error> move(Location newLocation) {
        if (newLocation == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("Location"));
        }

        this.location = newLocation;
        return UnitResult.success();
    }

    private Assignment findAssignment(UUID assignmentId) {
        return assignments.stream().filter(assignment -> assignment.getId().equals(assignmentId)).findFirst()
                .orElse(null);
    }

    private int getCurrentVolume() {
        return assignments.stream().mapToInt(assignment -> assignment.getVolume().getValue()).sum();
    }
}
