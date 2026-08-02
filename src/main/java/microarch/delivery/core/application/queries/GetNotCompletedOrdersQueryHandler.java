package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;

import java.util.Collection;

public interface GetNotCompletedOrdersQueryHandler {
    Result<Collection<GetNotCompletedOrdersResponse>, Error> handle(GetNotCompletedOrdersQuery query);
}
