package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.adapters.out.postgres.CourierRepositoryImpl;
import microarch.delivery.adapters.out.postgres.dto.AssignmentDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataClassRowMapper<CourierFlatRecord> courierMapper = DataClassRowMapper
            .newInstance(CourierFlatRecord.class);

    public GetAllCouriersQueryHandlerImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Result<Collection<GetAllCouriersResponse>, Error> handle(GetAllCouriersQuery query) {

        String sql = """
                SELECT id, name, location_x, location_y
                FROM couriers
                """;

        var couriers = jdbcTemplate.query(sql, Map.of(), courierMapper).stream()
                .map(row -> new GetAllCouriersResponse(row.id, row.name, new LocationDto(row.locationX, row.locationY)))
                .toList();

        return Result.success(couriers);
    }

    // Технический плоский рекорд для запроса значений атрибутов курьера, без связанных с ним списков
    private record CourierFlatRecord(UUID id, String name, int locationX, int locationY) {
    }
}