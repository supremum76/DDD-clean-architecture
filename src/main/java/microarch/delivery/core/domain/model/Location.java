package microarch.delivery.core.domain.model;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;

import java.util.List;

@Getter
public final class Location extends ValueObject<Location> {

    public static final int MIN_COORDINATE = 1;
    public static final int MAX_COORDINATE = 10;

    private final int x;
    private final int y;

    private Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Result<Location, Error> create(int x, int y) {
        Error error = Guard.combine(Guard.againstOutOfRange(x, MIN_COORDINATE, MAX_COORDINATE, "X"),
                Guard.againstOutOfRange(y, MIN_COORDINATE, MAX_COORDINATE, "Y"));

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Location(x, y));
    }

    /**
     * Возвращает расстояние до другой точки по манхэттенской метрике: сумма шагов перемещения от текущей точки к
     * целевой точке по оси X и по оси Y (движение только по вертикали и горизонтали).
     */
    public int distanceTo(Location other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    public boolean isNeighbor(Location other) {
        // проверяем, что other либо совпадает с точкой, либо соответствует одной из 8 смежных точек.
        return
                distanceTo(other) <= 1 ||
                distanceTo(other) == 2 && x != other.x && y != other.y;
    }
    
    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
