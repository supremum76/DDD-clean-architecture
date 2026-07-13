package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateOrderCommand {
    private final UUID orderId;
    private final Volume volume;
    private final Location location;

    public static Result<CreateOrderCommand, Error> create(
            UUID orderId,
            int volume,

            String country,
            String city,
            String street,
            String house,
            String apartment
    ) {
        var err = Guard.combine(
                Guard.againstNullOrEmpty(orderId, "orderId"),
                Guard.againstNullOrEmpty(country, "country"),
                Guard.againstNullOrEmpty(city, "city"),
                Guard.againstNullOrEmpty(street, "street"),
                Guard.againstNullOrEmpty(house, "house"),
                Guard.againstNullOrEmpty(apartment, "apartment"),
                Guard.againstLessOrEqual(volume, 1, "volume")
        );
        if (err != null)
            return Result.failure(err);

        var volumeResult = Volume.create(volume);
        if (volumeResult.isFailure())
            return Result.failure(volumeResult.getError());

        // TODO получать позицию заказа из сервиса Geo
        //  по данным переданного адреса
        var location = Location.create(
                ThreadLocalRandom.current().nextInt(Location.MIN_COORDINATE, Location.MAX_COORDINATE + 1),
                ThreadLocalRandom.current().nextInt(Location.MIN_COORDINATE, Location.MAX_COORDINATE + 1));

        return Result.success(new CreateOrderCommand(orderId, volumeResult.getValue(), location.getValue()));
    }
}
