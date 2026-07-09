package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.order.Order;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    void save(Order order);
    void update(Order order);

    Optional<Order> findById(UUID orderId);
    Optional<Order> findAnyCreated();
    Collection<Order> findAllAssigned();
}
