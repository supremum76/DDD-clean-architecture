package microarch.delivery.core.domain.services;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderErrors;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDispatchServiceTest {

    private static final UUID ASSIGNMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ORDER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID FIRST_COURIER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_COURIER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID THIRD_COURIER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private OrderDispatchService service;

    @BeforeEach
    void setUp() {
        service = new OrderDispatchServiceImpl();
    }

    @Test
    void dispatch_withAvailableCourier_assignsOrderAndReturnsClosestCourier() {
        Order order = createOrder(5, 5, 5);
        Courier closestCourier = createCourier(FIRST_COURIER_ID, 4, 5);
        Courier farCourier = createCourier(SECOND_COURIER_ID, 1, 1);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, order, List.of(farCourier, closestCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(closestCourier);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(closestCourier.getAssignments()).hasSize(1);
        assertThat(closestCourier.getAssignments().getFirst().getOrderId()).isEqualTo(ORDER_ID);
        assertThat(closestCourier.getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
        assertThat(farCourier.getAssignments()).isEmpty();
    }

    @Test
    void dispatch_skipsOverloadedCouriersAndSelectsAvailableOne() {
        Order order = createOrder(5, 5, 5);
        Courier overloadedCourier = createCourier(FIRST_COURIER_ID, 4, 5);
        overloadedCourier.takeOrder(UUID.fromString("66666666-6666-6666-6666-666666666666"),
                UUID.fromString("77777777-7777-7777-7777-777777777777"), Volume.create(20).getValue(),
                Location.create(1, 1).getValue());
        Courier availableCourier = createCourier(SECOND_COURIER_ID, 1, 1);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, order, List.of(overloadedCourier, availableCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(availableCourier);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(availableCourier.getAssignments()).hasSize(1);
    }

    @Test
    void dispatch_whenAllCouriersOverloaded_returnsNoSuitableCourierError() {
        Order order = createOrder(5, 5, 5);
        Courier firstOverloaded = createOverloadedCourier(FIRST_COURIER_ID, 1, 1);
        Courier secondOverloaded = createOverloadedCourier(SECOND_COURIER_ID, 2, 2);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, order, List.of(firstOverloaded, secondOverloaded));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(OrderDispatchErrors.NO_SUITABLE_COURIER);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void dispatch_whenCouriersListIsEmpty_returnsNoSuitableCourierError() {
        Order order = createOrder(5, 5, 5);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, order, List.of());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(OrderDispatchErrors.NO_SUITABLE_COURIER);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void dispatch_withNullOrder_returnsFailure() {
        Courier courier = createCourier(FIRST_COURIER_ID, 1, 1);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, null, List.of(courier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
    }

    @Test
    void dispatch_withNullCouriers_returnsFailure() {
        Order order = createOrder(5, 5, 5);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, order, null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
    }

    @Test
    void dispatch_withNullAssignmentId_returnsFailure() {
        Order order = createOrder(5, 5, 5);
        Courier courier = createCourier(FIRST_COURIER_ID, 1, 1);

        Result<Courier, ?> result = service.dispatch(null, order, List.of(courier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
    }

    @Test
    void dispatch_whenOrderIsNotCreated_returnsFailure() {
        Order order = createOrder(5, 5, 5);
        order.assign();
        Courier courier = createCourier(FIRST_COURIER_ID, 1, 1);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, order, List.of(courier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(OrderErrors.INVALID_STATUS_FOR_ASSIGN);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void dispatch_whenOrderVolumeDoesNotFitAnyCourier_returnsNoSuitableCourierError() {
        Order order = createOrder(5, 5, 21);
        Courier courier = createCourier(FIRST_COURIER_ID, 4, 5);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, order, List.of(courier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(OrderDispatchErrors.NO_SUITABLE_COURIER);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void dispatch_selectsCourierWithMinimumDistanceAmongAvailableOnes() {
        Order order = createOrder(5, 5, 5);
        Courier courierWithDistanceTwo = createCourier(FIRST_COURIER_ID, 5, 3);
        Courier courierWithDistanceOne = createCourier(SECOND_COURIER_ID, 4, 5);
        Courier courierWithDistanceThree = createCourier(THIRD_COURIER_ID, 2, 5);

        Result<Courier, ?> result = service.dispatch(ASSIGNMENT_ID, order,
                List.of(courierWithDistanceTwo, courierWithDistanceThree, courierWithDistanceOne));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(courierWithDistanceOne);
    }

    private Order createOrder(int x, int y, int volume) {
        return Order.create(ORDER_ID, Location.create(x, y).getValue(), Volume.create(volume).getValue()).getValue();
    }

    private Courier createCourier(UUID id, int x, int y) {
        return Courier.create(id, "Ivan", Location.create(x, y).getValue()).getValue();
    }

    private Courier createOverloadedCourier(UUID id, int x, int y) {
        Courier courier = createCourier(id, x, y);
        courier.takeOrder(UUID.fromString("66666666-6666-6666-6666-666666666666"),
                UUID.fromString("77777777-7777-7777-7777-777777777777"), Volume.create(20).getValue(),
                Location.create(1, 1).getValue());
        return courier;
    }
}
