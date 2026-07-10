package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import microarch.delivery.core.ports.CourierRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(CourierRepositoryImpl.class) // Явно импортируем наш репозиторий, так как @JdbcTest его не сканирует
class CourierRepositoryImplTest extends JdbcTestBaseConfig {

    @Autowired
    private CourierRepository courierRepository;

    private Courier testCourier;
    private Assignment assignment1;
    private Assignment assignment2;

    @BeforeEach
    void setUp() {
        // Инициализируем дочерние сущности (Assignments)
        // Примечание: Создаем изменяемый ArrayList, если ваш домен позволяет добавлять элементы
        List<Assignment> assignments = new ArrayList<>();

        assignment1 = Assignment.dto2domain(
                UUID.randomUUID(),
                UUID.randomUUID(), // orderId
                Volume.create(1).getValue(),
                Location.create(5, 7).getValue(),
                AssignmentStatus.ASSIGNED
        );

        assignment2 = Assignment.dto2domain(
                UUID.randomUUID(),
                UUID.randomUUID(), // orderId
                Volume.create(2).getValue(),
                Location.create(assignment1.getLocation().getX() + 1, assignment1.getLocation().getY()).getValue(),
                AssignmentStatus.ASSIGNED
        );

        assignments.add(assignment1);
        assignments.add(assignment2);

        // Инициализируем корневой агрегат (Courier)
        testCourier = Courier.dto2domain(
                UUID.randomUUID(),
                "courier123",
                Location.create(assignment1.getLocation().getX(), assignment1.getLocation().getY()).getValue(),
                assignments
        );
    }

    @Test
    @DisplayName("Должен успешно сохранить курьера вместе с его назначениями и найти его по ID")
    void shouldSaveAndFindCourierWithAssignments() {
        // Act (Сохраняем агрегат)
        courierRepository.save(testCourier);

        // Assert (Читаем агрегат обратно)
        Optional<Courier> foundCourierOpt = courierRepository.findById(testCourier.getId());

        assertThat(foundCourierOpt).isPresent();
        Courier foundCourier = foundCourierOpt.get();

        // 1. Проверяем поля самого курьера
        assertThat(foundCourier.getId()).isEqualTo(testCourier.getId());
        assertThat(foundCourier.getName()).isEqualTo(testCourier.getName());
        assertThat(foundCourier.getLocation()).isEqualTo(testCourier.getLocation());

        // 2. Проверяем вложенную коллекцию назначений
        assertThat(foundCourier.getAssignments())
                .hasSize(2)
                .extracting(Assignment::getId)
                .containsExactlyInAnyOrder(assignment1.getId(), assignment2.getId());

        // 3. Точечно проверяем маппинг полей конкретного назначения
        Assignment savedAssignment1 = foundCourier.getAssignments().stream()
                .filter(a -> a.getId().equals(assignment1.getId()))
                .findFirst()
                .orElseThrow();

        Assignment savedAssignment2 = foundCourier.getAssignments().stream()
                .filter(a -> a.getId().equals(assignment2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(savedAssignment1.getOrderId()).isEqualTo(assignment1.getOrderId());
        assertThat(savedAssignment1.getVolume()).isEqualTo(assignment1.getVolume());
        assertThat(savedAssignment1.getStatus()).isEqualTo(assignment1.getStatus());
        assertThat(savedAssignment1.getLocation()).isEqualTo(assignment1.getLocation());

        assertThat(savedAssignment2.getOrderId()).isEqualTo(assignment2.getOrderId());
        assertThat(savedAssignment2.getVolume()).isEqualTo(assignment2.getVolume());
        assertThat(savedAssignment2.getStatus()).isEqualTo(assignment2.getStatus());
        assertThat(savedAssignment2.getLocation()).isEqualTo(assignment2.getLocation());
    }

    @Test
    @DisplayName("Должен успешно обновить состояние курьера и список его назначений (изменение, удаление, добавление)")
    void shouldUpdateCourierAndItsAssignments() {
        courierRepository.save(testCourier);

        testCourier.completeAssignment(assignment1.getId());

        UUID newAssignmentId = UUID.randomUUID();
        testCourier.takeOrder(
                newAssignmentId,
                UUID.randomUUID(),
                Volume.create(3).getValue(),
                Location.create(testCourier.getLocation().getX(), testCourier.getLocation().getX()).getValue()
        );

        // Act
        courierRepository.update(testCourier);

        // Assert: Проверяем, что в базе отразились все изменения агрегата
        Optional<Courier> foundCourierOpt = courierRepository.findById(testCourier.getId());
        assertThat(foundCourierOpt).isPresent();
        Courier foundCourier = foundCourierOpt.get();

        assertThat(foundCourier.getName()).isEqualTo(testCourier.getName());
        assertThat(foundCourier.getLocation()).isEqualTo(testCourier.getLocation());

        // Назначений должно остаться 2 (одно старое измененное, одно новое, а второе старое должно удалиться)
        assertThat(foundCourier.getAssignments()).hasSize(2);

        // Проверяем, что в назначениях только еще не выполненные назначения
        Assignment savedAssignment2 = foundCourier.getAssignments().stream()
                .filter(a -> a.getId().equals(assignment2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(savedAssignment2.getStatus()).isEqualTo(assignment2.getStatus());
        assertThat(savedAssignment2.getLocation()).isEqualTo(assignment2.getLocation());

        // Проверяем, что новое назначение появилось
        Assignment newAssignment = foundCourier.getAssignments().stream()
                .filter(a -> a.getId().equals(newAssignmentId))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Должен вернуть всех курьеров со своими назначениями через findAll")
    void shouldFindAllCouriers() {
        // Arrange: Сохраняем первого курьера (у него 2 назначения)
        courierRepository.save(testCourier);

        // Создаем и сохраняем второго курьера (у него 0 назначений)
        Courier secondCourier = Courier.dto2domain(
                UUID.randomUUID(),
                "second courier",
                Location.create(1, 1).getValue(),
                List.of()
        );
        courierRepository.save(secondCourier);

        // Act
        Collection<Courier> allCouriers = courierRepository.findAll();

        // Assert
        assertThat(allCouriers).hasSize(2);

        Courier foundCourier1 = allCouriers.stream().filter(c -> c.getId().equals(testCourier.getId())).findFirst().orElseThrow();
        assertThat(foundCourier1.getAssignments()).hasSize(2);

        Courier foundCourier2 = allCouriers.stream().filter(c -> c.getId().equals(secondCourier.getId())).findFirst().orElseThrow();
        assertThat(foundCourier2.getAssignments()).isEmpty();
    }
}
