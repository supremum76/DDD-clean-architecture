package microarch.delivery.core.domain.services;

import libs.errs.Error;

public final class OrderDispatchErrors {

    public static final String NO_SUITABLE_COURIER = "order.dispatch.no.suitable.courier";

    private OrderDispatchErrors() {
    }

    public static Error noSuitableCourier() {
        return Error.of(NO_SUITABLE_COURIER, "No suitable courier found for order dispatch");
    }
}
