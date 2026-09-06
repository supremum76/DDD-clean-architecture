package microarch.delivery.adapters.out.postgres.outbox;

import libs.errs.Error;
import libs.errs.Guard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class OutboxMessage {
    private UUID id;

    private String eventType;

    private UUID aggregateId;

    private String aggregateType;

    private String payload;

    private Instant occurredOnUtc;

    private Instant processedOnUtc;

    public OutboxMessage(@NonNull UUID id, String eventType, UUID aggregateId, String aggregateType, String payload,
                         @NonNull Instant occurredOnUtc) {

        var err = Guard.combine(Guard.againstNullOrEmpty(eventType, "eventType"),
                Guard.againstNullOrEmpty(aggregateId, "aggregateId"),
                Guard.againstNullOrEmpty(aggregateType, "aggregateType"), Guard.againstNullOrEmpty(payload, "payload"));
        Error.throwIf(err);

        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.payload = payload;
        this.occurredOnUtc = occurredOnUtc;
    }

    public static OutboxMessage dto2domain(@NonNull UUID id, String eventType, UUID aggregateId, String aggregateType,
                                    String payload, @NonNull Instant occurredOnUtc, Instant processedOnUtc){
        var msg = new OutboxMessage(id, eventType, aggregateId, aggregateType, payload, occurredOnUtc);
        msg.processedOnUtc = processedOnUtc;
        return msg;
    }

    public void markAsProcessed() {
        this.processedOnUtc = Instant.now();
    }
}
