package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompleteOrderCommand {
    private final UUID courierId;
    private final UUID assignId;

    public static Result<CompleteOrderCommand, Error> create(UUID courierId, UUID assignId) {
        var err = Guard.combine(
                Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstNullOrEmpty(assignId, "assignId")
        );
        if (err != null)
            return Result.failure(err);

        return Result.success(new CompleteOrderCommand(courierId, assignId));
    }
}
