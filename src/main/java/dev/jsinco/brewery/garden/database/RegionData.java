package dev.jsinco.brewery.garden.database;

import dev.jsinco.brewery.garden.utility.vector.Vector2i;

import java.util.UUID;

public record RegionData(UUID regionId, Vector2i position) {
}
