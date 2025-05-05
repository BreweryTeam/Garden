CREATE TABLE IF NOT EXISTS garden_plants (
    id BINARY(16),
    plant_type VARCHAR(32),
    age INTEGER,
    bounding_box TEXT,
    world_uuid BINARY(16),
    PRIMARY KEY (uuid)
);

CREATE INDEX IF NOT EXISTS world_index
ON garden_plants(
    world_uuid
);

CREATE TABLE IF NOT EXISTS version
(
    version INTEGER DEFAULT -1,
    singleton_value DEFAULT 0,
    PRIMARY KEY (singleton_value)
);