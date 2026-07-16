package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {
    private final CourierRepository courierRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, DomainEventPublisher domainEventPublisher) {
        this.courierRepository = courierRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var courierOpt = courierRepository.findById(command.getCourierId());
        if (courierOpt.isEmpty())
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        var courier = courierOpt.get();

        var completeResult = courier.completeAssignment(command.getAssignmentId());
        if(completeResult.isFailure())
            return UnitResult.failure(completeResult.getError());

        courierRepository.update(courier);
        domainEventPublisher.publish(List.of(courier));

        return UnitResult.success();
    }
}