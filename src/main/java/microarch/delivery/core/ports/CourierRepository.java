package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.courier.Courier;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface CourierRepository {
    void save(Courier courier);

    void update(Courier courier);

    Optional<Courier> findById(UUID courierId);

    Collection<Courier> findAll();
}