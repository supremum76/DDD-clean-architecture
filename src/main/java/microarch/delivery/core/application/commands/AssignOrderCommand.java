package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;

public final class AssignOrderCommand {
    private final static Result<AssignOrderCommand, Error> assignOrderCommandResult
            = Result.success(new AssignOrderCommand());

    private AssignOrderCommand(){}

    public static Result<AssignOrderCommand, Error> create() {
        return assignOrderCommandResult;
    }
}
