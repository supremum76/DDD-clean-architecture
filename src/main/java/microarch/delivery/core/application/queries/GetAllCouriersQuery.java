package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;

public final class GetAllCouriersQuery {
    private static final Result<GetAllCouriersQuery, Error> queryResult = Result.success(new GetAllCouriersQuery());

    private GetAllCouriersQuery() {
    };

    public static Result<GetAllCouriersQuery, Error> create() {
        return queryResult;
    }
}
