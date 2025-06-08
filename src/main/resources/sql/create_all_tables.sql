CREATE TABLE IF NOT EXISTS plants (
    id BINARY(16),
    plant_type VARCHAR(32),
    age INTEGER,
    origin_x INTEGER,
    origin_y INTEGER,
    origin_z INTEGER,
    world_uuid BINARY(16),
    transformation TEXT,
    track TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS world_index
ON plants(
    world_uuid
);

CREATE TABLE IF NOT EXISTS version
(
    version INTEGER DEFAULT -1,
    singleton_value DEFAULT 0,
    PRIMARY KEY (singleton_value)
);