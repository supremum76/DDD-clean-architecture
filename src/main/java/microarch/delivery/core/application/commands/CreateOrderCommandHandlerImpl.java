package microarch.delivery.core.application.commands;

import java.util.List;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
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
    private final GeoClient geoClient;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository, DomainEventPublisher domainEventPublisher, GeoClient geoClient) {
        this.orderRepository = orderRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.geoClient = geoClient;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateOrderCommand command) {
        var location = geoClient.getGeolocation(
                command.getCountry(),
                command.getCity(),
                command.getStreet(),
                command.getHouse(),
                command.getApartment()
        );

        var orderResult = Order.create(command.getOrderId(), location, command.getVolume());
        if (orderResult.isFailure())
            return UnitResult.failure(orderResult.getError());

        var order = orderResult.getValue();

        orderRepository.save(order);
        domainEventPublisher.publish(List.of(order));

        return UnitResult.success();
    }
}