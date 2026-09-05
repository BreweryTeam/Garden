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
    fruits INTEGER,
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

CREATE TABLE IF NOT EXISTS regions
(
    region_id BINARY(16),
    region_x INTEGER,
    region_z INTEGER,
    world_uuid BINARY(16),
    PRIMARY KEY(region_id),
    UNIQUE (region_x, region_y, region_z, world_uuid)
);

CREATE TABLE IF NOT EXISTS region_plant_relation
(
    region_id BINARY(16),
    plant_id BINARY(16),
    FOREIGN KEY(region_id) REFERENCES regions(region_id) ON DELETE CASCADE,
    FOREIGN KEY(plant_id) REFERENCES plants(id) ON DELETE CASCADE
)