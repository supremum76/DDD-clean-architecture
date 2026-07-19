package microarch.delivery.core.application.commands;

import java.util.List;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {
    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository, DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateOrderCommand command) {
        var orderResult = Order.create(command.getOrderId(), command.getLocation(), command.getVolume());
        if (orderResult.isFailure())
            return UnitResult.failure(orderResult.getError());

        var order = orderResult.getValue();

        orderRepository.save(order);
        domainEventPublisher.publish(List.of(order));

        return UnitResult.success();
    }
}