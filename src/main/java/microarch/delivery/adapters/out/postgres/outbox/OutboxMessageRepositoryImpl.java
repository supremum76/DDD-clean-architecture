package microarch.delivery.adapters.out.postgres.outbox;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Repository
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class OutboxMessageRepositoryImpl implements OutboxMessageRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    // Создаем мапперы один раз. Он автоматически свяжет колонки SQL с полями Dto
    private final DataClassRowMapper<OutboxMessageRepositoryImpl.OutboxMessageDTO> outboxMessageMapper = DataClassRowMapper
            .newInstance(OutboxMessageRepositoryImpl.OutboxMessageDTO.class);

    // Спринг автоматически внедрит jdbcTemplate
    public OutboxMessageRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void save(OutboxMessage message) {
        String sql = """
                INSERT INTO outbox(
                        id,
                        event_type,
                        aggregate_id,
                        aggregate_type,
                        payload,
                        occurred_on_utc,
                        processed_on_utc,
                        is_processed)
                VALUES(
                        :id,
                        :event_type,
                        :aggregate_id,
                        :aggregate_type,
                        :payload,
                        :occurred_on_utc,
                        :processed_on_utc,
                        :is_processed)
                """;

        var params = new MapSqlParameterSource()
                .addValue("id", message.getId())
                .addValue("event_type", message.getEventType())
                .addValue("aggregate_id", message.getAggregateId())
                .addValue("aggregate_type", message.getAggregateType())
                .addValue("payload", message.getPayload())
                //.addValue("occurred_on_utc", message.getOccurredOnUtc(), Types.TIMESTAMP)
                //.addValue("processed_on_utc", message.getProcessedOnUtc(), Types.TIMESTAMP)
                .addValue("occurred_on_utc", message.getOccurredOnUtc() != null ? message.getOccurredOnUtc().atOffset(ZoneOffset.UTC) : null)
                .addValue("processed_on_utc", message.getProcessedOnUtc() != null ? message.getProcessedOnUtc().atOffset(ZoneOffset.UTC) : null)
                .addValue("is_processed", message.getProcessedOnUtc() != null)
                ;

        jdbcTemplate.update(sql, params);
    }

    @Override
    @Transactional
    public void update(OutboxMessage message) {
        String sql = """
                UPDATE outbox
                    SET
                        processed_on_utc = :processed_on_utc,
                        is_processed = :is_processed
                WHERE id = :id
                """;

        var params = new MapSqlParameterSource()
                .addValue("id", message.getId())
                //.addValue("processed_on_utc", message.getProcessedOnUtc(), Types.TIMESTAMP)
                .addValue("processed_on_utc", message.getProcessedOnUtc() != null ? message.getProcessedOnUtc().atOffset(ZoneOffset.UTC) : null)
                .addValue("is_processed", message.getProcessedOnUtc() != null)
                ;

        jdbcTemplate.update(sql, params);
    }

    @Override
    @Transactional
    public Collection<OutboxMessage> findUnprocessedMessages() {
        String sql = """
                    SELECT
                        id,
                        event_type,
                        aggregate_id,
                        aggregate_type,
                        payload,
                        occurred_on_utc,
                        processed_on_utc
                    FROM
                        outbox
                    WHERE
                        NOT is_processed
                    ORDER BY occurred_on_utc ASC
                    LIMIT 10000
                """;

        return jdbcTemplate
                .query(sql, Map.of(), outboxMessageMapper)
                .stream()
                .map(row -> OutboxMessage.dto2domain(
                        row.id,
                        row.eventType,
                        row.aggregateId,
                        row.aggregateType,
                        row.payload,
                        row.occurredOnUtc,
                        row.processedOnUtc)
                ).toList();
    }

    // Технический плоский рекорд для запроса значений атрибутов курьера, без связанных с ним списков
    private record OutboxMessageDTO(
            UUID id,
            String eventType,
            UUID aggregateId,
            String aggregateType,
            String payload,
            Instant occurredOnUtc,
            Instant processedOnUtc) {}
}
