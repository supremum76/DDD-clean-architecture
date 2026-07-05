package microarch.delivery.adapters.out.postgres;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@SqlResultSetMapping(
        name = OrderMappingEntity.MAPPING_NAME,
        classes = @ConstructorResult(
                targetClass = OrderRowDto.class, // Промежуточный DTO для сбора примитивов
                columns = {
                        @ColumnResult(name = "id", type = UUID.class),
                        @ColumnResult(name = "status", type = Integer.class),
                        @ColumnResult(name = "volume", type = Integer.class),
                        @ColumnResult(name = "location_x", type = Integer.class),
                        @ColumnResult(name = "location_y", type = Integer.class)
                }
        )
)
public class OrderMappingEntity {
    public static final String MAPPING_NAME = "OrderDomainMapping";

    @Id
    private UUID id; // Минимальное требование для компиляции @Entity
}
