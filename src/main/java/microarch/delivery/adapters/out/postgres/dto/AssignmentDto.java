package microarch.delivery.adapters.out.postgres.dto;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;

import java.util.UUID;

public record AssignmentDto(UUID id, UUID orderId, int status, int volume, int locationX, int locationY) {
    public Assignment toDomain() {
        return Assignment.dto2domain(this.id, this.orderId, Volume.create(this.volume).getValueOrThrow(),
                Location.create(this.locationX, this.locationY).getValueOrThrow(),
                AssignmentStatus.fromCode(this.status));
    }
}
