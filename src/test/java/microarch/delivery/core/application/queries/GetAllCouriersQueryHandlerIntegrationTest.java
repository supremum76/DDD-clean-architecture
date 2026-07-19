package microarch.delivery.core.application.queries;

import microarch.delivery.adapters.out.postgres.CourierRepositoryImpl;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({CourierRepositoryImpl.class, GetAllCouriersQueryHandlerImpl.class}) // Явно импортируем наш репозиторий, так как @JdbcTest его не сканирует
class GetAllCouriersQueryHandlerIntegrationTest extends JdbcTestBaseConfig {
    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    GetAllCouriersQueryHandlerImpl handler;

    @Test
    void testGetAllCouriers_NoCouriers() {
        // Act
        var query = GetAllCouriersQuery.create().getValue();
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var response = result.getValue();
        assertThat(response.size()).isEqualTo(0);
    }

    @Test
    void testGetAllCouriers_TwoCouriers() {
        // Arrange
        var courier1 = Courier.create(
                UUID.randomUUID(),
                "courier1",
                Location.create(1, 2).getValue()
        ).getValue();

        var courier2 = Courier.create(
                UUID.randomUUID(),
                "courier2",
                Location.create(3, 4).getValue()
        ).getValue();

        courierRepository.save(courier1);
        courierRepository.save(courier2);

        // Act
        var query = GetAllCouriersQuery.create().getValue();
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var response = result.getValue();
        assertThat(response.size()).isEqualTo(2);
        assertThat(
                response
                        .stream()
                        .filter(a -> a.courierId().equals(courier1.getId()))
                        .toList()
        ).hasSize(1);
        assertThat(
                response
                        .stream()
                        .filter(a -> a.courierId().equals(courier2.getId()))
                        .toList()
        ).hasSize(1);
    }
}