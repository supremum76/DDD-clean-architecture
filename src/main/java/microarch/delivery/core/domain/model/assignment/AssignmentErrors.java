package microarch.delivery.core.domain.model.assignment;

import libs.errs.Error;

public final class AssignmentErrors {

    public static final String ALREADY_COMPLETED = "assignment.already.completed";
    public static final String COURIER_TOO_FAR_FROM_ORDER_LOCATION = "assignment.courier.too.far.from.order.location";

    private AssignmentErrors() {
    }

    public static Error alreadyCompleted() {
        return Error.of(ALREADY_COMPLETED, "Assignment is already completed");
    }

    public static Error courierTooFarFromOrderLocation() {
        return Error.of(COURIER_TOO_FAR_FROM_ORDER_LOCATION,
                "Courier must be in the same cell as the order location to complete the assignment");
    }
}
