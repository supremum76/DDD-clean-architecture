package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import libs.ddd.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Job {
    private final DomainEventProducer producer;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    /* Использование fixedDelay гарантирует, что всегда выполняется не более одного экземляра задачи в рамках
    одного экземпляра приложения. */
    @Scheduled(fixedDelay = 1_000)
    public void run() {
        var outboxMessages = outboxMessageRepository.findUnprocessedMessages();
        for (var outboxMessage : outboxMessages) {
            try {
                var eventClass = Class.forName(outboxMessage.getEventType());
                var eventObject = objectMapper.readValue(outboxMessage.getPayload(), eventClass);

                if (!(eventObject instanceof DomainEvent domainEvent)) {
                    throw new IllegalStateException("Invalid outbox message type: " + eventClass);
                }

                producer.produce(domainEvent);

                outboxMessage.markAsProcessed();
                outboxMessageRepository.update(outboxMessage);
            } catch (Exception e) {
                log.error("Failed to publish outbox message {}", outboxMessage.getId(), e);
            }
        }
    }
}
