package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.ports.OrderRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void save(Order order) {
        em.createNativeQuery("insert into orders(id, status, volume, location_x, location_y) select " +
                "values(:id, :status, :volume, :location_x, :location_y)")
                .setParameter("id", order.getId())
                .setParameter("status", order.getStatus().getCode())
                .setParameter("volume", order.getVolume().getValue())
                .setParameter("location_x", order.getLocation().getX())
                .setParameter("location_y", order.getLocation().getY())
                .executeUpdate();
    }

    @Override
    @Transactional
    public void update(Order order) {
        em.createNativeQuery("update orders set " +
                        "status = :status," +
                        "volume = :volume," +
                        "location_x = :location_x," +
                        "location_y = :location_y " +
                        "where id = :id")
                .setParameter("id", order.getId())
                .setParameter("status", order.getStatus().getCode())
                .setParameter("volume", order.getVolume().getValue())
                .setParameter("location_x", order.getLocation().getX())
                .setParameter("location_y", order.getLocation().getY())
                .executeUpdate();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Object findById(UUID orderId) {
        var query = em.createQuery(
                "select id, status, volume, location_x, location_y " +
                        "from orders where id = :id");
        query.setParameter("id", orderId);

        try(Stream stream = query.getResultStream()) {
            Optional rowOpt = stream.findFirst();

            if (rowOpt.isPresent()) {
                Object[] row = (Object[]) rowOpt.get();

                var id = (UUID) row[0];
                var status = OrderStatus.values()[(int) row[1]];
                var volume = Volume.create((int) row[2]).getValue();
                var location_x = (int) row[3];
                var location_y = (int) row[4];

                return Optional.of(new Order(id, Location.create(location_x, location_y).getValue(), volume, status));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Order> findAnyCreated() {
        var query = em.createQuery(
                "select id, status, volume, location_x, location_y " +
                        "from orders where status = :created_code");
        query.setParameter("created_code", OrderStatus.CREATED.getCode());

        try(Stream stream = query.getResultStream()) {
            Optional rowOpt = stream.findFirst();

            if (rowOpt.isPresent()) {
                Object[] row = (Object[]) rowOpt.get();

                var id = (UUID) row[0];
                var status = OrderStatus.values()[(int) row[1]];
                var volume = Volume.create((int) row[2]).getValue();
                var location_x = (int) row[3];
                var location_y = (int) row[4];

                return Optional.of(new Order(id, Location.create(location_x, location_y).getValue(), volume, status));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<Order>> findAllAssigned() {
        var query = em.createQuery(
                "select id, status, volume, location_x, location_y " +
                        "from orders where status = :assigned_code");
        query.setParameter("assigned_code", OrderStatus.ASSIGNED.getCode());

        try(Stream stream = query.getResultStream()) {
            List orders =  stream.map(row -> {
                var id = (UUID) ((Object[]) row)[0];
                var status = OrderStatus.values()[(int) ((Object[]) row)[1]];
                var volume = Volume.create((int) ((Object[]) row)[2]).getValue();
                var location_x = (int) ((Object[]) row)[3];
                var location_y = (int) ((Object[]) row)[4];

                return new Order(id, Location.create(location_x, location_y).getValue(), volume, status);
            }).toList();

            if(!orders.isEmpty())
                return Optional.of((List<Order>) orders);
        }

        return Optional.empty();
    }
}
