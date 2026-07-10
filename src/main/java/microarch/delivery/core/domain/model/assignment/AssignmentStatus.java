package microarch.delivery.core.domain.model.assignment;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AssignmentStatus {
    ASSIGNED(1), COMPLETED(2);

    private final int code;

    private AssignmentStatus(int code) {
        this.code = code;
    }

    public static AssignmentStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown assignment status code " + code));
    }

}
