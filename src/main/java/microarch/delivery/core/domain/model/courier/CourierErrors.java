package microarch.delivery.core.domain.model.courier;

import libs.errs.Error;

public final class CourierErrors {

    public static final String CANNOT_TAKE_ORDER = "courier.cannot.take.order";
    public static final String ASSIGNMENT_NOT_FOUND = "courier.assignment.not.found";
    public static final String ASSIGNMENT_CANNOT_BE_COMPLETED = "courier.assignment.cannot.be.completed";

    private CourierErrors() {
    }

    public static Error cannotTakeOrder() {
        return Error.of(CANNOT_TAKE_ORDER,
                "Courier cannot take the order because the total volume would exceed the maximum allowed volume");
    }

    public static Error assignmentNotFound() {
        return Error.of(ASSIGNMENT_NOT_FOUND, "Assignment was not found for the courier");
    }

    public static Error assignmentCannotBeCompleted() {
        return Error.of(ASSIGNMENT_CANNOT_BE_COMPLETED,
                "The courier assignment can't be completed due to the courier and order location.");
    }
}
