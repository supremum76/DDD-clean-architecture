package microarch.delivery.adapters.out.postgres;

import microarch.delivery.adapters.out.postgres.dto.OrderDto;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class OrderRepositoryImpl implements OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    // Создаем маппер один раз. Он автоматически свяжет колонки SQL с полями OrderDto
    private final DataClassRowMapper<OrderDto> orderMapper = DataClassRowMapper.newInstance(OrderDto.class);

    // Спринг автоматически внедрит jdbcTemplate
    public OrderRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void save(Order order) {
        String sql = """
                INSERT INTO orders(id, status, volume, location_x, location_y)
                VALUES(:id, :status, :volume, :location_x, :location_y)
                """;

        var params = new MapSqlParameterSource().addValue("id", order.getId())
                .addValue("status", order.getStatus().getCode()).addValue("volume", order.getVolume().getValue())
                .addValue("location_x", order.getLocation().getX()).addValue("location_y", order.getLocation().getY());

        jdbcTemplate.update(sql, params);
    }

    @Override
    @Transactional
    public void update(Order order) {
        String sql = """
                UPDATE orders
                SET
                    status = :status,
                    volume = :volume,
                    location_x = :location_x,
                    location_y = :location_y
                WHERE id = :id
                """;

        var params = new MapSqlParameterSource().addValue("id", order.getId())
                .addValue("status", order.getStatus().getCode()).addValue("volume", order.getVolume().getValue())
                .addValue("location_x", order.getLocation().getX()).addValue("location_y", order.getLocation().getY());

        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        String sql = """
                    SELECT id, status, volume, location_x, location_y
                    FROM orders
                    WHERE id = :id
                """;

        List<OrderDto> results = jdbcTemplate.query(sql, Map.of("id", orderId), orderMapper);

        return results.stream().map(OrderDto::toDomain).findFirst();
    }

    @Override
    public Optional<Order> findAnyCreated() {
        String sql = """
                SELECT id, status, volume, location_x, location_y
                FROM orders
                WHERE status = :created_code
                """;

        List<OrderDto> results = jdbcTemplate.query(sql, Map.of("created_code", OrderStatus.CREATED.getCode()),
                orderMapper);

        return results.stream().map(OrderDto::toDomain).findFirst();
    }

    @Override
    public Collection<Order> findAllAssigned() {
        String sql = """
                SELECT id, status, volume, location_x, location_y
                FROM orders
                WHERE status = :assigned_code
                """;

        List<OrderDto> results = jdbcTemplate.query(sql, Map.of("assigned_code", OrderStatus.ASSIGNED.getCode()),
                orderMapper);

        return results.stream().map(OrderDto::toDomain).toList();
    }
}
