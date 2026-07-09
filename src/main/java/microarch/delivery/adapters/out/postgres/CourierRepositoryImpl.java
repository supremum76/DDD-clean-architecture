package microarch.delivery.adapters.out.postgres;

import microarch.delivery.adapters.out.postgres.dto.AssignmentDto;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class CourierRepositoryImpl implements CourierRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    // Создаем мапперы один раз. Он автоматически свяжет колонки SQL с полями Dto
    private final DataClassRowMapper<CourierFlatRecord> courierMapper =
            DataClassRowMapper.newInstance(CourierFlatRecord.class);
    private final DataClassRowMapper<AssignmentDto> assignmentMapper =
            DataClassRowMapper.newInstance(AssignmentDto.class);
    private final DataClassRowMapper<CourierAllDataFlatRecord> courierAllDataMapper =
            DataClassRowMapper.newInstance(CourierAllDataFlatRecord.class);

    // Спринг автоматически внедрит jdbcTemplate
    public CourierRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void save(Courier courier) {
        String sql = """
                INSERT INTO couriers(id, name, location_x, location_y)
                VALUES(:id, :name, :location_x, :location_y)
                """;

        var params = new MapSqlParameterSource()
                .addValue("id", courier.getId())
                .addValue("name", courier.getName())
                .addValue("location_x", courier.getLocation().getX())
                .addValue("location_y", courier.getLocation().getY());

        jdbcTemplate.update(sql, params);

        updateAssignments(courier.getId(), courier.getAssignments());
    }

    @Override
    @Transactional
    public void update(Courier courier) {
        String sql = """
                UPDATE couriers
                SET
                    name = :name,
                    location_x = :location_x,
                    location_y = :location_y
                WHERE id = :id
                """;

        var params = new MapSqlParameterSource()
                .addValue("id", courier.getId())
                .addValue("name", courier.getName())
                .addValue("location_x", courier.getLocation().getX())
                .addValue("location_y", courier.getLocation().getY());

        jdbcTemplate.update(sql, params);

        updateAssignments(courier.getId(), courier.getAssignments());
    }

    @Override
    public Optional<Courier> findById(UUID courierId) {
        String sqlCourier = "SELECT id, name, location_x, location_y FROM couriers WHERE id = :id";

        Optional<CourierFlatRecord> courierOpt = jdbcTemplate
                .query(sqlCourier, Map.of("id", courierId), courierMapper)
                .stream()
                .findFirst();

        if(courierOpt.isEmpty()) return Optional.empty();
        var courier = courierOpt.get();

        String sqlAssignments = """
            SELECT id, order_id, status, volume, location_x, location_y
            FROM assignments
            WHERE
                    courier_id = :courier_id
                AND status = :assigned_status
        """;

        List<Assignment> assignments = jdbcTemplate.query(
                sqlAssignments,
                Map.of(
                        "courier_id", courierId,
                        "complete_status", AssignmentStatus.ASSIGNED.getCode()
                ),
                        assignmentMapper
                )
                .stream().map(AssignmentDto::toDomain).toList();

        return Optional.of(
                Courier.dto2domain(
                        courier.id,
                        courier.name,
                        Location.create(courier.locationX, courier.locationY).getValueOrThrow(),
                        assignments
                )
        );

    }

    @Override
    public Collection<Courier> findAll() {
        String sql = """
            SELECT
                couriers.id AS courier_id,
                couriers.name,
                couriers.location_x AS courier_location_x,
                couriers.location_y AS courier_location_y,
        
                assignments.id AS assignment_id,
                assignments.order_id,
                assignments.status,
                assignments.volume,
                assignments.location_x AS assignment_location_x,
                assignments.location_y AS assignment_location_y
            FROM
                couriers
                LEFT JOIN assignments ON
                        couriers.id = assignments.courier_id
                    AND assignments.status = :assigned_status
            ORDER BY courier_id
        """;

        return jdbcTemplate.query(
                        sql,
                        Map.of("assigned_status", AssignmentStatus.ASSIGNED.getCode()),
                        courierAllDataMapper
                )
                .stream()
                .map(row ->
                        Courier.dto2domain(
                                row.courierId,
                                row.name,
                                Location.create(row.courierLocationX, row.courierLocationY).getValueOrThrow(),
                                List.of(
                                        Assignment.dto2domain(
                                                row.assignmentId,
                                                row.orderId,
                                                Volume.create(row.volume).getValueOrThrow(),
                                                Location.create(row.assignmentLocationX, row.assignmentLocationY).getValueOrThrow(),
                                                AssignmentStatus.fromCode(row.status)
                                        )
                                )
                        )
                )
                .collect(
                        Collectors.toMap(
                                Courier::getId,
                                Function.identity(),
                                (existing, replacement) ->
                                        Courier.dto2domain(
                                                existing.getId(),
                                                existing.getName(),
                                                existing.getLocation(),
                                                Stream.concat(existing.getAssignments().stream(), replacement.getAssignments().stream()).toList()
                                        )
                        )
                ).values();
    }

    @Transactional
    private void updateAssignments(UUID courierId, Collection<Assignment> assignments){
        final var dummy_params = new MapSqlParameterSource();

        jdbcTemplate.update("""
                    CREATE TEMPORARY TABLE temp_assignments (
                        id UUID NOT NULL,
                        order_id UUID NOT NULL,
                        status int NOT NULL,
                        volume int NOT NULL,
                        location_x int NOT NULL,
                        location_y int NOT NULL
                    ) ON COMMIT DROP
                    """,
                dummy_params);

        String sqlFillTemporaryTable = """
                INSERT INTO temp_assignments(id, order_id, status, volume, location_x, location_y)
                VALUES(:id, :orderId, :status, :volume, :locationX, :locationY)
                """;
        SqlParameterSource[] batchArgs = SqlParameterSourceUtils.createBatch(
                assignments.stream().map(assignment -> new AssignmentDto(
                        assignment.getId(),
                        assignment.getOrderId(),
                        assignment.getStatus().getCode(),
                        assignment.getVolume().getValue(),
                        assignment.getLocation().getX(),
                        assignment.getLocation().getY()
                        )
                ).toList()
        );
        jdbcTemplate.batchUpdate(sqlFillTemporaryTable, batchArgs);

        String sqlComplete = """
                UPDATE assignments
                SET status = :complete_code
                WHERE
                        courier_id = :courier_id
                    AND status != :complete_code
                    AND id NOT IN(SELECT id FROM temp_assignments)
                """;
        jdbcTemplate.update(
                sqlComplete,
                new MapSqlParameterSource()
                        .addValue("courier_id", courierId)
                        .addValue("complete_code", AssignmentStatus.COMPLETED.getCode())
        );

        String sqlInsert = """
                INSERT INTO assignments(id, courier_id, order_id, status, volume, location_x, location_y)
                SELECT id, :courier_id, order_id, status, volume, location_x, location_y
                FROM temp_assignments
                ON CONFLICT (id) DO NOTHING
                """;
        jdbcTemplate.update(
                sqlInsert,
                new MapSqlParameterSource().addValue("courier_id", courierId)
        );
    }

    // Технический плоский рекорд для запроса значений атрибутов курьера, без связанных с ним списков
    private record CourierFlatRecord(UUID id, String name, int locationX, int locationY) {}

    // Технический плоский рекорд для запроса значений атрибутов курьера и связанных с ним назначений заказов
    private record CourierAllDataFlatRecord(
            UUID courierId,
            String name,
            int courierLocationX,
            int courierLocationY,

            UUID assignmentId,
            UUID orderId,
            int status,
            int volume,
            int assignmentLocationX,
            int assignmentLocationY
    ) {}
}