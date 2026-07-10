CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    status INT NOT NULL,
    volume INT NOT NULL,
    -- location
    location_x INT NOT NULL,
    location_y INT NOT NULL
);

CREATE TABLE IF NOT EXISTS couriers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    -- location
    location_x INT NOT NULL,
    location_y INT NOT NULL
);

CREATE TABLE IF NOT EXISTS assignments (
    id UUID PRIMARY KEY,

    courier_id UUID NOT NULL REFERENCES couriers(id) ON DELETE CASCADE,
    order_id UUID NOT NULL,

    volume INT NOT NULL,

    -- location
    location_x INT NOT NULL,
    location_y INT NOT NULL,

    status INT NOT NULL
);