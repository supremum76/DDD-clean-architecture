package microarch.delivery.core.domain.model.order;

import libs.errs.Error;

public final class OrderErrors {

    public static final String INVALID_STATUS_FOR_ASSIGN = "order.invalid.status.for.assign";
    public static final String INVALID_STATUS_FOR_COMPLETE = "order.invalid.status.for.complete";

    private OrderErrors() {
    }

    public static Error invalidStatusForAssign() {
        return Error.of(INVALID_STATUS_FOR_ASSIGN, "Order can be assigned only when its status is Created");
    }

    public static Error invalidStatusForComplete() {
        return Error.of(INVALID_STATUS_FOR_COMPLETE, "Order can be completed only when its status is Assigned");
    }
}
