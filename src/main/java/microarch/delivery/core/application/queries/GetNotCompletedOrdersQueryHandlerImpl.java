package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Service
public class GetNotCompletedOrdersQueryHandlerImpl implements GetNotCompletedOrdersQueryHandler {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataClassRowMapper<OrderFlatRecord> orderMapper =
            DataClassRowMapper.newInstance(OrderFlatRecord.class);

    public GetNotCompletedOrdersQueryHandlerImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Result<Collection<GetNotCompletedOrdersResponse>, Error> handle(GetNotCompletedOrdersQuery query) {

        String sql = """
                SELECT id, name, location_x, location_y
                FROM orders
                WHERE status != :completed_code
                """;

        var orders = jdbcTemplate.query(
                        sql,
                        Map.of("completed_code", OrderStatus.COMPLETED.getCode()),
                        orderMapper
                )
                .stream()
                .map(row -> new GetNotCompletedOrdersResponse(
                        row.id,
                        new LocationDto(row.locationX, row.locationY)))
                .toList();

        return Result.success(orders);
    }

    // Технический плоский рекорд для запроса значений атрибутов курьера, без связанных с ним списков
    private record OrderFlatRecord(UUID id, int locationX, int locationY) {}
}