package microarch.delivery.core.domain.model.order;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum OrderStatus {
    CREATED(1), ASSIGNED(2), COMPLETED(3);

    private final int code;

    private OrderStatus(int code) {
        this.code = code;
    }

    public static OrderStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown order status code " + code));
    }
}
