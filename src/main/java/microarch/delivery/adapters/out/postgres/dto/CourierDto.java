package microarch.delivery.adapters.out.postgres.dto;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.courier.Courier;

import java.util.List;
import java.util.UUID;

public record CourierDto(UUID id, String name, int locationX, int locationY, List<Assignment> assignments) {

    public Courier toDomain() {
        return Courier.dto2domain(
                this.id,
                this.name,
                Location.create(this.locationX, this.locationY).getValueOrThrow(),
                this.assignments
        );
    }
}