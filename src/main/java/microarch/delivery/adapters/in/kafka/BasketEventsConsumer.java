package microarch.delivery.adapters.in.kafka;

import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import queues.basket.events.BasketEventsProto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasketEventsConsumer {
    private final CreateOrderCommandHandler createOrderCommandHandler;

    @KafkaListener(topics = "${app.kafka.basket-events-topic}")
    public void listen(byte[] message) {
        try {
            var event = BasketEventsProto.BasketConfirmedIntegrationEvent.parseFrom(message);

            // Создаем команду
            var address = event.getAddress();
            var createCommandResult = CreateOrderCommand.create(
                    UUID.randomUUID(),
                    event.getVolume(),
                    address.getCountry(),
                    address.getCity(),
                    address.getStreet(),
                    address.getHouse(),
                    address.getApartment());

            if (createCommandResult.isFailure()) {
                throw new RuntimeException("Invalid command: " + createCommandResult.getError());
            }
            var command = createCommandResult.getValue();

            // Обрабатываем команду
            var handleCommandResult = this.createOrderCommandHandler.handle(command);
            if (handleCommandResult.isFailure()) {
                throw new RuntimeException("Failed to handle command: " + handleCommandResult.getError());
            }

        } catch (com.google.protobuf.InvalidProtocolBufferException ex) {
            throw new RuntimeException("Failed to parse protobuf message", ex);
        }
    }
}