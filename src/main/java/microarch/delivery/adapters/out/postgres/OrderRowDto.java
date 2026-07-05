package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import java.util.UUID;

// Spring Data JDBC может автоматически мапить строки таблицы в Java Records, если имена полей совпадают
// с именами колонок (snake_case автоматически переводится в camelCase).
public record OrderRowDto(UUID id, int status, int volume, int locationX, int locationY) {

    public Order toDomain() {
        return new Order(
                this.id,
                Location.create(this.locationX, this.locationY).getValueOrThrow(),
                Volume.create(this.volume).getValueOrThrow(),
                OrderStatus.fromCode(this.status)
        );
    }
}
