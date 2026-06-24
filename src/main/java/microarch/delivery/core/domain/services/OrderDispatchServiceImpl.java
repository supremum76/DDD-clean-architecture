package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderErrors;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderDispatchServiceImpl implements OrderDispatchService {

    @Override
    public Result<Courier, Error> dispatch(UUID assignmentId, Order order, List<Courier> couriers) {
        Error error = Guard.combine(Guard.againstNullOrEmpty(assignmentId, "AssignmentId"),
                order == null ? GeneralErrors.valueIsRequired("Order") : null,
                couriers == null ? GeneralErrors.valueIsRequired("Couriers") : null,
                order != null && order.getStatus() != OrderStatus.CREATED ? OrderErrors.invalidStatusForAssign()
                        : null);

        if (error != null) {
            return Result.failure(error);
        }

        Courier selectedCourier = selectClosestAvailableCourier(order, couriers);
        if (selectedCourier == null) {
            return Result.failure(OrderDispatchErrors.noSuitableCourier());
        }

        UnitResult<Error> takeOrderResult = selectedCourier.takeOrder(assignmentId, order.getId(), order.getVolume(),
                order.getLocation());
        if (takeOrderResult.isFailure()) {
            return Result.failure(takeOrderResult.getError());
        }

        UnitResult<Error> assignResult = order.assign();
        if (assignResult.isFailure()) {
            return Result.failure(assignResult.getError());
        }

        return Result.success(selectedCourier);
    }

    private Courier selectClosestAvailableCourier(Order order, List<Courier> couriers) {
        Courier selectedCourier = null;
        int minDistance = Integer.MAX_VALUE;

        for (Courier courier : couriers) {
            if (!courier.canBeAssigned(order.getVolume())) {
                continue;
            }

            int distance = courier.getLocation().distanceTo(order.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                selectedCourier = courier;
            }
        }

        return selectedCourier;
    }
}
