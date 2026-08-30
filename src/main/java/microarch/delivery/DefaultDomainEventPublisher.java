package microarch.delivery;

import libs.ddd.Aggregate;
import libs.ddd.DomainEvent;
import libs.ddd.DomainEventPublisher;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.stereotype.Component;

@Component
public class DefaultDomainEventPublisher implements DomainEventPublisher {
    private final DomainEventProducer publisher;

    public DefaultDomainEventPublisher( DomainEventProducer publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(Iterable<? extends Aggregate<?>> aggregates) {
        for (var aggregate : aggregates) {
            for (var event : aggregate.getDomainEvents()) {
                publisher.produce(event);
            }
            aggregate.clearDomainEvents();
        }
    }
}