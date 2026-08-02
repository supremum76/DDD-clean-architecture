package microarch.delivery.adapters.out.grpc;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import microarch.delivery.ApplicationProperties;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.ports.GeoClient;
import org.springframework.stereotype.Service;

@Service
public class GeoClientImpl implements GeoClient {
    private final ManagedChannel channel;
    private final GeoGrpc.GeoBlockingStub stub;

    public GeoClientImpl(ApplicationProperties properties) {
        var GeoService = properties.getGrpc().getGeoService();

        this.channel = ManagedChannelBuilder
                .forAddress(
                        GeoService.getHost(),
                        GeoService.getPort()
                )
                .usePlaintext()
                .build();

        this.stub = GeoGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (!channel.isShutdown()) {
            channel.shutdown();
        }
    }

    @Override
    public Location getGeolocation(String country, String city, String street, String house, String apartment) {
        // Проект демонстрационный, поэтому для урощения передается только улица
        var request = GeoProto
                .GetGeolocationRequest
                .newBuilder()
                .setStreet(street)
                .build();

        var response = stub.getGeolocation(request);

        return Location.create(
                response.getLocation().getX(),
                response.getLocation().getY()
        ).getValueOrThrow();
    }
}
