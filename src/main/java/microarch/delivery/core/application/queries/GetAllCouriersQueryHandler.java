package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;

import java.util.Collection;

public interface GetAllCouriersQueryHandler {
    Result<Collection<GetAllCouriersResponse>, Error> handle(GetAllCouriersQuery query);
}
