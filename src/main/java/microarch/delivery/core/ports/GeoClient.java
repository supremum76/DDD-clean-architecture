package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.Location;

public interface GeoClient {
    Location getGeolocation(String country, String city, String street, String house, String apartment);
}