package dev.jsinco.brewery.garden.registry;

import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.utility.vector.Vector2i;
import dev.jsinco.brewery.garden.utility.vector.Vector3i;
import org.bukkit.Chunk;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlantRegion {

    private final UUID regionId;
    private final Map<Vector3i, GardenPlant> plants = new ConcurrentHashMap<>();
    private final Set<UUID> plantIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public PlantRegion(UUID regionId) {
        this.regionId = regionId;
    }

    public void register(GardenPlant gardenPlant, List<Vector3i> positions) {
        positions.forEach(position -> plants.put(position, gardenPlant));
    }

    public void register(GardenPlant gardenPlant, Vector3i position) {
        plants.put(position, gardenPlant);
        plantIds.add(gardenPlant.getId());
    }

    public void unregister(Vector3i pos) {
        GardenPlant gardenPlant = plants.remove(pos);
        if (gardenPlant != null) {
            plantIds.remove(gardenPlant.getId());
        }
    }

    public Optional<GardenPlant> getPlant(Vector3i position) {
        return Optional.ofNullable(plants.get(position));
    }

    public boolean isEmpty() {
        return plants.isEmpty();
    }

    public Collection<GardenPlant> contents() {
        return plants.values();
    }

    public UUID regionId() {
        return this.regionId;
    }

    public Collection<UUID> plantIds() {
        return plantIds;
    }

    public void unload() {
        plants.clear();
    }

    public static Vector2i toRegionCoords(Vector2i vector2i) {
        return new Vector2i(vector2i.x() >> 4, vector2i.z() >> 4);
    }

    public static Vector2i toRegionCoords(Vector3i vector3i) {
        return new Vector2i(vector3i.x() >> 4, vector3i.z() >> 4);
    }

    public static Vector2i toRegionCoords(Chunk chunk) {
        return new Vector2i(chunk.getX(), chunk.getZ());
    }

}
