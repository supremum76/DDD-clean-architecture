package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryImplTest extends BaseConfig {
    @Autowired
    private OrderRepositoryImpl orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Инициализируем тестовый объект перед каждым тестом
        testOrder = new Order(
                UUID.randomUUID(),
                Location.create(3, 7).getValue(),
                Volume.create(100).getValue(),
                OrderStatus.CREATED
        );
    }

    @Test
    @DisplayName("Должен успешно сохранить и найти заказ по ID")
    void shouldSaveAndFindOrderById() {
        // Act (Действие)
        orderRepository.save(testOrder);
        Optional<Order> foundOrderOpt = orderRepository.findById(testOrder.getId());

        // Assert (Проверка)
        assertThat(foundOrderOpt).isPresent();
        Order foundOrder = foundOrderOpt.get();

        assertThat(foundOrder.getId()).isEqualTo(testOrder.getId());
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(foundOrder.getVolume().getValue()).isEqualTo(testOrder.getVolume().getValue());
        assertThat(foundOrder.getLocation().getX()).isEqualTo(testOrder.getLocation().getX());
        assertThat(foundOrder.getLocation().getY()).isEqualTo(testOrder.getLocation().getY());
    }

    @Test
    @DisplayName("Должен успешно обновить существующий заказ")
    void shouldUpdateOrder() {
        // Arrange
        orderRepository.save(testOrder);

        Order updatedOrder = new Order(
                testOrder.getId(),
                Location.create(testOrder.getLocation().getX(), testOrder.getLocation().getY()).getValue(),
                Volume.create(testOrder.getVolume().getValue()).getValue(),
                OrderStatus.ASSIGNED // Меняем статус
        );

        // Act
        orderRepository.update(updatedOrder);
        Optional<Order> foundOrderOpt = orderRepository.findById(testOrder.getId());

        // Assert
        assertThat(foundOrderOpt).isPresent();
        Order foundOrder = foundOrderOpt.get();
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(foundOrder.getVolume().getValue()).isEqualTo(testOrder.getVolume().getValue());
        assertThat(foundOrder.getLocation().getX()).isEqualTo(testOrder.getLocation().getX());
        assertThat(foundOrder.getLocation().getY()).isEqualTo(testOrder.getLocation().getY());
    }

    @Test
    @DisplayName("Должен вернуть любой созданный заказ (findAnyCreated)")
    void shouldFindAnyCreatedOrder() {
        // Arrange
        orderRepository.save(testOrder);

        // Act
        Optional<Order> createdOrderOpt = orderRepository.findAnyCreated();

        // Assert
        assertThat(createdOrderOpt).isPresent();
        assertThat(createdOrderOpt.get().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("Должен вернуть список всех назначенных заказов (findAllAssigned)")
    void shouldFindAllAssignedOrders() {
        // Arrange
        Order assignedOrder1 = new Order(
                UUID.randomUUID(),
                Location.create(1, 1).getValue(),
                Volume.create(10).getValue(),
                OrderStatus.ASSIGNED);
        Order assignedOrder2 = new Order(
                UUID.randomUUID(),
                Location.create(2, 2).getValue(),
                Volume.create(20).getValue(),
                OrderStatus.ASSIGNED);

        orderRepository.save(testOrder); // Этот со статусом CREATED, он не должен попасть в выборку
        orderRepository.save(assignedOrder1);
        orderRepository.save(assignedOrder2);

        // Act
        List<Order> assignedOrders = orderRepository.findAllAssigned();

        // Assert
        assertThat(assignedOrders)
                .hasSize(2)
                .extracting(Order::getId)
                .containsExactlyInAnyOrder(assignedOrder1.getId(), assignedOrder2.getId());
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если назначенных заказов нет")
    void shouldReturnEmptyListWhenNoAssignedOrders() {
        // Arrange
        orderRepository.save(testOrder); // Только CREATED

        // Act
        List<Order> assignedOrders = orderRepository.findAllAssigned();

        // Assert
        assertThat(assignedOrders).isEmpty();
    }
}
