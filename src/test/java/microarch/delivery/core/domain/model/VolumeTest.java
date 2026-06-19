package microarch.delivery.core.domain.model;

import libs.errs.GeneralErrors;
import libs.errs.Result;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VolumeTest {

    @Test
    void create_withPositiveValue_returnsSuccess() {
        Result<Volume, ?> result = Volume.create(5);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(5);
    }

    @Test
    void create_withZero_returnsFailure() {
        Result<Volume, ?> result = Volume.create(0);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_MUST_BE_GREATER_OR_EQUAL);
    }

    @Test
    void create_withNegativeValue_returnsFailure() {
        Result<Volume, ?> result = Volume.create(-1);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo(GeneralErrors.VALUE_MUST_BE_GREATER_OR_EQUAL);
    }

    @Test
    void equals_withSameValue_returnsTrue() {
        Volume first = Volume.create(10).getValue();
        Volume second = Volume.create(10).getValue();

        assertThat(first).isEqualTo(second);
    }
}
