package microarch.delivery.core.domain.model;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void create_withValidCoordinates_returnsSuccess() {
        Result<Location, ?> result = Location.create(5, 7);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(5);
        assertThat(result.getValue().getY()).isEqualTo(7);
    }

    @Test
    void create_withMinimumCoordinates_returnsSuccess() {
        Result<Location, ?> result = Location.create(1, 1);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(1);
        assertThat(result.getValue().getY()).isEqualTo(1);
    }

    @Test
    void create_withMaximumCoordinates_returnsSuccess() {
        Result<Location, ?> result = Location.create(10, 10);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(10);
        assertThat(result.getValue().getY()).isEqualTo(10);
    }

    @Test
    void create_withXBelowMinimum_returnsFailure() {
        Result<Location, ?> result = Location.create(0, 5);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_OUT_OF_RANGE);
    }

    @Test
    void create_withXAboveMaximum_returnsFailure() {
        Result<Location, ?> result = Location.create(11, 5);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_OUT_OF_RANGE);
    }

    @Test
    void create_withYBelowMinimum_returnsFailure() {
        Result<Location, ?> result = Location.create(5, 0);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_OUT_OF_RANGE);
    }

    @Test
    void create_withYAboveMaximum_returnsFailure() {
        Result<Location, ?> result = Location.create(5, 11);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_IS_OUT_OF_RANGE);
    }

    @Test
    void equals_withSameCoordinates_returnsTrue() {
        Location first = Location.create(3, 4).getValue();
        Location second = Location.create(3, 4).getValue();

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void equals_withDifferentCoordinates_returnsFalse() {
        Location first = Location.create(3, 4).getValue();
        Location second = Location.create(3, 5).getValue();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void distanceTo_returnsManhattanDistance() {
        Location courier = Location.create(2, 3).getValue();
        Location order = Location.create(4, 6).getValue();

        assertThat(courier.distanceTo(order)).isEqualTo(5);
    }

    @Test
    void distanceTo_withSameLocation_returnsZero() {
        Location location = Location.create(5, 5).getValue();

        assertThat(location.distanceTo(location)).isZero();
    }

    @Test
    void compareTo_ordersByXThenY() {
        Location first = Location.create(1, 5).getValue();
        Location second = Location.create(2, 1).getValue();
        Location third = Location.create(2, 3).getValue();

        assertThat(first).isLessThan(second);
        assertThat(second).isLessThan(third);
    }
}
