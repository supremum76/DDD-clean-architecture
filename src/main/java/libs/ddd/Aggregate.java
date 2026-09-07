package libs.ddd;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MappedSuperclass
public abstract class Aggregate extends BaseEntity implements AggregateRoot<UUID> {

    @Transient
    protected List<DomainEvent> domainEvents;

    protected Aggregate() {
        this.domainEvents = new ArrayList<>();
    }

    protected Aggregate(UUID id) {
        super(id);
        this.domainEvents = new ArrayList<>();
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public void raiseDomainEvent(DomainEvent domainEvent) {
        if (domainEvents == null) {
            domainEvents = new ArrayList<>();
        }
        domainEvents.add(domainEvent);
    }
}