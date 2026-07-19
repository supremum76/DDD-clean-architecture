package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {
    private final CourierRepository courierRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CreateCourierCommandHandlerImpl(CourierRepository courierRepository, DomainEventPublisher domainEventPublisher) {
        this.courierRepository = courierRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateCourierCommand command) {
        var courierResult = Courier.create(command.getCourierId(), command.getName(), command.getLocation());
        if (courierResult.isFailure())
            return UnitResult.failure(courierResult.getError());

        var courier = courierResult.getValue();

        courierRepository.save(courier);
        domainEventPublisher.publish(List.of(courier));

        return UnitResult.success();
    }
}