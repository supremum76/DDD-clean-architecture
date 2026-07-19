package microarch.delivery.core.application.queries;

import microarch.delivery.adapters.out.postgres.CourierRepositoryImpl;
import microarch.delivery.adapters.out.postgres.OrderRepositoryImpl;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({OrderRepositoryImpl.class, CourierRepositoryImpl.class, GetNotCompletedOrdersQueryHandlerImpl.class}) // Явно импортируем наш репозиторий, так как @JdbcTest его не сканирует
class GetNotCompletedOrdersQueryHandlerIntegrationTest extends JdbcTestBaseConfig {
    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    GetNotCompletedOrdersQueryHandlerImpl handler;

    @Test
    void testGetNotCompletedOrders_NoOrders() {
        // Act
        var query = GetNotCompletedOrdersQuery.create().getValue();
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var response = result.getValue();
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    void testGetNotCompletedOrders_ExistsOrders() {
        // Arrange
        var courier = Courier.create(
                UUID.randomUUID(),
                "courier123",
                Location.create(1, 2).getValue()
        ).getValue();

        courierRepository.save(courier);

        var order1 = Order.create(
                UUID.randomUUID(),
                courier.getLocation(),
                Volume.create(1).getValue()
        ).getValue();

        var order2 = Order.create(
                UUID.randomUUID(),
                courier.getLocation(),
                Volume.create(1).getValue()
        ).getValue();

        var order3 = Order.create(
                UUID.randomUUID(),
                courier.getLocation(),
                Volume.create(1).getValue()
        ).getValue();

        courier.takeOrder(
                UUID.randomUUID(),
                order2.getId(),
                order2.getVolume(),
                order2.getLocation()
        );
        order2.assign();

        var assignmentId = UUID.randomUUID();
        courier.takeOrder(
                assignmentId,
                order3.getId(),
                order3.getVolume(),
                order3.getLocation()
        );
        order3.assign();
        courier.completeAssignment(assignmentId);
        order3.complete();

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        // Act
        var query = GetNotCompletedOrdersQuery.create().getValue();
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var response = result.getValue();
        assertThat(response.size()).isEqualTo(2);
        assertThat(
                response
                        .stream()
                        .filter(a -> a.orderId().equals(order1.getId()))
                        .toList()
        ).hasSize(1);
        assertThat(
                response
                        .stream()
                        .filter(a -> a.orderId().equals(order2.getId()))
                        .toList()
        ).hasSize(1);
        assertThat(
                response
                        .stream()
                        .filter(a -> a.orderId().equals(order3.getId()))
                        .toList()
        ).hasSize(0);
    }
}