-- V1__create_vehicles_table.sql
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER,
    mileage INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'VERFUEGBAR',
    tour_number VARCHAR(20),
    driver VARCHAR(100),
    next_inspection DATE,
    next_oil_change DATE,
    last_inspection DATE,
    location VARCHAR(100),
    notes TEXT
);

-- Index für häufige Suchen
CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_driver ON vehicles(driver);
CREATE INDEX idx_vehicles_model ON vehicles(model);
