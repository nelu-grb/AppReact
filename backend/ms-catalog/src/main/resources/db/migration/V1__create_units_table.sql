CREATE TABLE IF NOT EXISTS UNITS (
    unit_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    address VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    availability BOOLEAN DEFAULT TRUE,
    rooms INT NOT NULL,
    bathrooms INT NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    max_occupancy INT NOT NULL
);