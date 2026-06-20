package microarch.delivery.core.domain.model.order;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void create_withValidArguments_returnsOrderWithCreatedStatus() {
        Location location = Location.create(3, 4).getValue();
        Volume volume = Volume.create(5).getValue();

        Result<Order, ?> result = Order.create(ORDER_ID, location, volume);

        assertThat(result.isSuccess()).isTrue();
        Order order = result.getValue();
        assertThat(order.getId()).isEqualTo(ORDER_ID);
        assertThat(order.getLocation()).isEqualTo(location);
        assertThat(order.getVolume()).isEqualTo(volume);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void create_withNullId_returnsFailure() {
        Location location = Location.create(3, 4).getValue();
        Volume volume = Volume.create(5).getValue();

        Result<Order, ?> result = Order.create(null, location, volume);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
    }

    @Test
    void create_withNullLocation_returnsFailure() {
        Volume volume = Volume.create(5).getValue();

        Result<Order, ?> result = Order.create(ORDER_ID, null, volume);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
    }

    @Test
    void create_withNullVolume_returnsFailure() {
        Location location = Location.create(3, 4).getValue();

        Result<Order, ?> result = Order.create(ORDER_ID, location, null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
    }

    @Test
    void assign_whenStatusIsCreated_returnsSuccess() {
        Order order = createOrder();

        UnitResult<?> result = order.assign();

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void assign_whenStatusIsNotCreated_returnsFailure() {
        Order order = createOrder();
        order.assign();

        UnitResult<?> result = order.assign();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(OrderErrors.INVALID_STATUS_FOR_ASSIGN);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void complete_whenStatusIsAssigned_returnsSuccess() {
        Order order = createOrder();
        order.assign();

        UnitResult<?> result = order.complete();

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void complete_whenStatusIsCreated_returnsFailure() {
        Order order = createOrder();

        UnitResult<?> result = order.complete();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(OrderErrors.INVALID_STATUS_FOR_COMPLETE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void complete_whenAlreadyCompleted_returnsFailure() {
        Order order = createOrder();
        order.assign();
        order.complete();

        UnitResult<?> result = order.complete();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(OrderErrors.INVALID_STATUS_FOR_COMPLETE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void equals_withSameId_returnsTrue() {
        Order first = createOrder();
        Order sameIdDifferentData = Order
                .create(ORDER_ID, Location.create(9, 9).getValue(), Volume.create(9).getValue()).getValue();

        assertThat(first).isEqualTo(sameIdDifferentData);

        Order withDifferentId = Order.create(UUID.fromString("22222222-2222-2222-2222-222222222222"),
                Location.create(1, 1).getValue(), Volume.create(1).getValue()).getValue();

        assertThat(first).isNotEqualTo(withDifferentId);
    }

    private Order createOrder() {
        return Order.create(ORDER_ID, Location.create(3, 4).getValue(), Volume.create(5).getValue()).getValue();
    }
}
