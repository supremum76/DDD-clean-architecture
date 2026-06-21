package microarch.delivery.core.domain.model.courier;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourierTest {

    private static final UUID COURIER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ASSIGNMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ORDER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void create_withValidArguments_returnsCourier() {
        Location location = Location.create(2, 3).getValue();

        Result<Courier, ?> result = Courier.create(COURIER_ID, "Ivan", location);

        assertThat(result.isSuccess()).isTrue();
        Courier courier = result.getValue();
        assertThat(courier.getId()).isEqualTo(COURIER_ID);
        assertThat(courier.getName()).isEqualTo("Ivan");
        assertThat(courier.getLocation()).isEqualTo(location);
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void create_withNullName_returnsFailure() {
        Location location = Location.create(2, 3).getValue();

        Result<Courier, ?> result = Courier.create(COURIER_ID, null, location);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
    }

    @Test
    void canBeAssigned_whenVolumeFits_returnsTrue() {
        Courier courier = createCourier(1, 1);

        assertThat(courier.canBeAssigned(Volume.create(20).getValue())).isTrue();
    }

    @Test
    void canBeAssigned_whenVolumeExceedsMaximum_returnsFalse() {
        Courier courier = createCourier(1, 1);

        assertThat(courier.canBeAssigned(Volume.create(21).getValue())).isFalse();
    }

    @Test
    void takeOrder_withValidArguments_addsAssignmentToList() {
        Courier courier = createCourier(1, 1);
        Location orderLocation = Location.create(3, 4).getValue();
        Volume volume = Volume.create(5).getValue();

        UnitResult<?> result = courier.takeOrder(ASSIGNMENT_ID, ORDER_ID, volume, orderLocation);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
        Assignment assignment = courier.getAssignments().getFirst();
        assertThat(assignment.getId()).isEqualTo(ASSIGNMENT_ID);
        assertThat(assignment.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(assignment.getVolume()).isEqualTo(volume);
        assertThat(assignment.getLocation()).isEqualTo(orderLocation);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void takeOrder_whenVolumeExceedsMaximum_returnsFailure() {
        Courier courier = createCourier(1, 1);

        UnitResult<?> result = courier.takeOrder(ASSIGNMENT_ID, ORDER_ID, Volume.create(21).getValue(),
                Location.create(3, 4).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(CourierErrors.CANNOT_TAKE_ORDER);
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void takeOrder_whenTotalVolumeExceedsMaximum_returnsFailure() {
        Courier courier = createCourier(1, 1);
        courier.takeOrder(ASSIGNMENT_ID, ORDER_ID, Volume.create(15).getValue(), Location.create(3, 4).getValue());

        UnitResult<?> result = courier.takeOrder(UUID.fromString("44444444-4444-4444-4444-444444444444"),
                UUID.fromString("55555555-5555-5555-5555-555555555555"), Volume.create(6).getValue(),
                Location.create(5, 6).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(CourierErrors.CANNOT_TAKE_ORDER);
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void completeAssignment_whenCourierNearOrder_removesAssignmentFromList() {
        Courier courier = createCourier(3, 4);
        courier.takeOrder(ASSIGNMENT_ID, ORDER_ID, Volume.create(5).getValue(), Location.create(3, 4).getValue());

        UnitResult<?> result = courier.completeAssignment(ASSIGNMENT_ID);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void completeAssignment_whenCourierTooFar_returnsFailure() {
        Courier courier = createCourier(1, 1);
        courier.takeOrder(ASSIGNMENT_ID, ORDER_ID, Volume.create(5).getValue(), Location.create(5, 6).getValue());

        UnitResult<?> result = courier.completeAssignment(ASSIGNMENT_ID);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(CourierErrors.ASSIGNMENT_CANNOT_BE_COMPLETED);
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void completeAssignment_whenAssignmentNotFound_returnsFailure() {
        Courier courier = createCourier(3, 4);

        UnitResult<?> result = courier.completeAssignment(ASSIGNMENT_ID);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(CourierErrors.ASSIGNMENT_NOT_FOUND);
    }

    @Test
    void move_withValidLocation_changesCourierLocation() {
        Courier courier = createCourier(1, 1);
        Location newLocation = Location.create(5, 5).getValue();

        UnitResult<?> result = courier.move(newLocation);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(newLocation);
    }

    @Test
    void move_withNullLocation_returnsFailure() {
        Courier courier = createCourier(1, 1);

        UnitResult<?> result = courier.move(null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
        assertThat(courier.getLocation()).isEqualTo(Location.create(1, 1).getValue());
    }

    @Test
    void equals_withSameId_returnsTrue() {
        Courier first = createCourier(1, 1);
        Courier sameIdDifferentData = Courier
                .create(COURIER_ID, "Petr", Location.create(9, 9).getValue()).getValue();

        assertThat(first).isEqualTo(sameIdDifferentData);

        Courier withDifferentId = Courier.create(UUID.fromString("66666666-6666-6666-6666-666666666666"), "Ivan",
                Location.create(1, 1).getValue()).getValue();

        assertThat(first).isNotEqualTo(withDifferentId);
    }

    private Courier createCourier(int x, int y) {
        return Courier.create(COURIER_ID, "Ivan", Location.create(x, y).getValue()).getValue();
    }
}
