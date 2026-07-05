package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
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
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class OrderRepositoryImpl implements OrderRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Override
    public void save(Order order) {
        em.createNativeQuery("insert into orders(id, status, volume, location_x, location_y) " +
                "values(:id, :status, :volume, :location_x, :location_y)")
                .setParameter("id", order.getId())
                .setParameter("status", order.getStatus().getCode())
                .setParameter("volume", order.getVolume().getValue())
                .setParameter("location_x", order.getLocation().getX())
                .setParameter("location_y", order.getLocation().getY())
                .executeUpdate();
    }

    @Transactional
    @Override
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
    public Optional<Order> findById(UUID orderId) {
        // Передаем имя маппинга вторым аргументом
        var query = em.createNativeQuery(
                "select id, status, volume, location_x, location_y from orders where id = :id",
                OrderMappingEntity.MAPPING_NAME
        );
        query.setParameter("id", orderId);

        try (Stream<?> stream = query.getResultStream()) {
            return stream.map(dto -> ((OrderRowDto) dto).toDomain()).findFirst();
        }
    }

    @Override
    public Optional<Order> findAnyCreated() {
        var query = em.createNativeQuery(
                "select id, status, volume, location_x, location_y from orders where status = :created_code",
                OrderMappingEntity.MAPPING_NAME
        );
        query.setParameter("created_code", OrderStatus.CREATED.getCode());

        try (Stream<OrderRowDto> stream = query.getResultStream()) {
            return stream.map(OrderRowDto::toDomain).findFirst();
        }
    }

    @Override
    public List<Order> findAllAssigned() {
        var query = em.createNativeQuery(
                "select id, status, volume, location_x, location_y " +
                        "from orders where status = :assigned_code",
                OrderMappingEntity.MAPPING_NAME
        );
        query.setParameter("assigned_code", OrderStatus.ASSIGNED.getCode());

        try (Stream<OrderRowDto> stream = query.getResultStream()) {
            return stream.map(OrderRowDto::toDomain).toList();
        }
    }
}
