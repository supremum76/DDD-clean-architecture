package microarch.delivery.core.domain.model.assignment;

import lombok.Getter;

@Getter
public enum AssignmentStatus {
    ASSIGNED(1), COMPLETED(2);

    private final int code;

    private AssignmentStatus(int code) {
        this.code = code;
    }

}
