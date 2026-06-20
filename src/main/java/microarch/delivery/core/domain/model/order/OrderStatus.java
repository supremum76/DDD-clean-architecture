package microarch.delivery.core.domain.model.order;

import lombok.Getter;

@Getter
public enum OrderStatus {
    CREATED(1), ASSIGNED(2), COMPLETED(3);

    private final int code;

    private OrderStatus(int code) {
        this.code = code;
    }
}
