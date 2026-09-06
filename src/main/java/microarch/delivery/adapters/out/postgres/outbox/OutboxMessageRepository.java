package microarch.delivery.adapters.out.postgres.outbox;

import java.util.Collection;

public interface OutboxMessageRepository {
    void save(OutboxMessage message);
    void update(OutboxMessage message);
    Collection<OutboxMessage> findUnprocessedMessages();
}
