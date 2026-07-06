package microarch.delivery.adapters.out.postgres;

import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;


@Testcontainers // Включает поддержку Testcontainers в JUnit 5
@JdbcTest       // Запускает только слой работы с JDBC
@Import(OrderRepositoryImpl.class) // Явно импортируем наш репозиторий, так как @JdbcTest его не сканирует
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Отключаем дефолтную H2 встроенную базу
abstract class BaseConfig {

    // Автоматически запускает контейнер Постгреса и связывает его со Spring Datasource через @ServiceConnection
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource(
                                    "init-db.sql"), "/docker-entrypoint-initdb.d/"
                    );
}