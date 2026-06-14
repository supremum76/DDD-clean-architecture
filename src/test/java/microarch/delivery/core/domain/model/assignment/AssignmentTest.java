package microarch.delivery.core.domain.model.assignment;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentTest {

    private static final UUID ASSIGNMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void create_withValidArguments_returnsAssignmentWithAssignedStatus() {
        Location orderLocation = Location.create(3, 4).getValue();
        Volume volume = Volume.create(2).getValue();

        Result<Assignment, ?> result = Assignment.create(ASSIGNMENT_ID, ORDER_ID, volume, orderLocation);

        assertThat(result.isSuccess()).isTrue();
        Assignment assignment = result.getValue();
        assertThat(assignment.getId()).isEqualTo(ASSIGNMENT_ID);
        assertThat(assignment.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(assignment.getVolume()).isEqualTo(volume);
        assertThat(assignment.getLocation()).isEqualTo(orderLocation);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void create_withNullOrderId_returnsFailure() {
        Location orderLocation = Location.create(3, 4).getValue();
        Volume volume = Volume.create(2).getValue();

        Result<Assignment, ?> result = Assignment.create(ASSIGNMENT_ID, null, volume, orderLocation);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_REQUIRED);
    }

    @Test
    void complete_whenCourierInSameCell_returnsSuccess() {
        Assignment assignment = createAssignment(3, 4);
        Location courierLocation = Location.create(3, 4).getValue();

        UnitResult<?> result = assignment.complete(courierLocation);

        assertThat(result.isSuccess()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void complete_whenCourierTooFar_returnsFailure() {
        Assignment assignment = createAssignment(3, 4);
        Location courierLocation = Location.create(5, 6).getValue();

        UnitResult<?> result = assignment.complete(courierLocation);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(AssignmentErrors.COURIER_TOO_FAR_FROM_ORDER_LOCATION);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void complete_whenAlreadyCompleted_returnsFailure() {
        Assignment assignment = createAssignment(3, 4);
        Location courierLocation = Location.create(3, 4).getValue();
        assignment.complete(courierLocation);

        UnitResult<?> result = assignment.complete(courierLocation);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(AssignmentErrors.ALREADY_COMPLETED);
    }

    @Test
    void equals_withSameId_returnsTrue() {
        Assignment first = createAssignment(ASSIGNMENT_ID, 1, 1);
        Assignment sameIdDifferentData = Assignment
                .create(ASSIGNMENT_ID, UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        Volume.create(9).getValue(), Location.create(9, 9).getValue())
                .getValue();

        assertThat(first).isEqualTo(sameIdDifferentData);

        Assignment withDifferentId = createAssignment(UUID.fromString("44444444-4444-4444-4444-444444444444"), 1, 1);

        assertThat(first).isNotEqualTo(withDifferentId);
    }

    private Assignment createAssignment(int x, int y) {
        return createAssignment(ASSIGNMENT_ID, x, y);
    }

    private Assignment createAssignment(UUID id, int x, int y) {
        return Assignment.create(id, ORDER_ID, Volume.create(1).getValue(), Location.create(x, y).getValue())
                .getValue();
    }
}
