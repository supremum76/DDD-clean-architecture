package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.services.OrderDispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderDispatchService orderDispatchService;
    private final DomainEventPublisher domainEventPublisher;

    public AssignOrderCommandHandlerImpl(OrderRepository orderRepository, CourierRepository courierRepository,
            OrderDispatchService orderDispatchService, DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.orderDispatchService = orderDispatchService;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrderCommand command) {
        var orderOpt = orderRepository.findAnyCreated();
        if (orderOpt.isEmpty()) // Если нет нераспределенных заказов
            return UnitResult.success();

        var order = orderOpt.get();
        var couriers = courierRepository.findAll();

        var courierResult = orderDispatchService.dispatch(UUID.randomUUID(), order, couriers);
        if (courierResult.isFailure())
            return UnitResult.failure(courierResult.getError());
        var courier = courierResult.getValue();

        orderRepository.update(order);
        courierRepository.update(courier);

        domainEventPublisher.publish(List.of(order, courier));

        return UnitResult.success();
    }
}