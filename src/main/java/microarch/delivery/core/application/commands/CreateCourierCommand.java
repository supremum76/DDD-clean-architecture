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
public final class CreateCourierCommand {
    private final UUID courierId;
    private final String name;
    private final Location location;

    public static Result<CreateCourierCommand, Error> create(
            UUID courierId,
            String name
    ) {
        var err = Guard.combine(
                Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstNullOrEmpty(name, "name")
        );
        if (err != null)
            return Result.failure(err);

        // TODO заменить случайную позицию на фактическую
        var location = Location.create(
                ThreadLocalRandom.current().nextInt(Location.MIN_COORDINATE, Location.MAX_COORDINATE + 1),
                ThreadLocalRandom.current().nextInt(Location.MIN_COORDINATE, Location.MAX_COORDINATE + 1));

        return Result.success(new CreateCourierCommand(courierId, name, location.getValue()));
    }
}
