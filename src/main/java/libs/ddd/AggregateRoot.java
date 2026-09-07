package libs.ddd;

import java.util.List;
import java.util.UUID;

public interface AggregateRoot<ID extends UUID> {
    ID getId();

    List<DomainEvent> getDomainEvents();

    void clearDomainEvents();
}