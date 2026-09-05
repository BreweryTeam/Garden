package dev.jsinco.brewery.garden.registry;

import dev.jsinco.brewery.garden.database.RegionData;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.utility.vector.Vector2i;
import dev.jsinco.brewery.garden.utility.vector.Vector3i;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlantRegistry {
    private final Map<UUID, Map<Vector2i, PlantRegion>> gardenPlants = new ConcurrentHashMap<>();
    private final Map<UUID, GardenPlant> gardenPlantIds = new ConcurrentHashMap<>();


    public Optional<GardenPlant> getByID(UUID id) {
        return Optional.ofNullable(gardenPlantIds.get(id));
    }

    public Optional<GardenPlant> getByLocation(Block block) {
        World world = block.getWorld();
        Vector3i pos = Vector3i.from(block);
        Vector2i regionPos = PlantRegion.toRegionCoords(pos);
        return Optional.ofNullable(gardenPlants.computeIfAbsent(
                        world.getUID(),
                        ignored -> new ConcurrentHashMap<>()
                ).get(regionPos)
        ).flatMap(plantRegion -> plantRegion.getPlant(pos));
    }

    public void registerPlant(GardenPlant plant) {
        Map<Vector2i, PlantRegion> regions = gardenPlants.computeIfAbsent(
                plant.getStructure().worldUuid(), ignored -> new ConcurrentHashMap<>()
        );
        for (Location location : plant.getStructure().locations()) {
            Vector3i pos = Vector3i.from(location);
            Vector2i regionPos = PlantRegion.toRegionCoords(pos);
            PlantRegion plantRegion = regions.get(regionPos);
            if (plantRegion == null) {
                plantRegion = new PlantRegion(UUID.randomUUID());
                regions.put(regionPos, plantRegion);
            }
            plantRegion.register(plant, pos);
        }
        gardenPlantIds.put(plant.getId(), plant);
    }

    public Set<Vector2i> unregisterPlant(GardenPlant plant) {
        gardenPlantIds.remove(plant.getId());
        Map<Vector2i, PlantRegion> regions = gardenPlants.computeIfAbsent(
                plant.getStructure().worldUuid(),
                ignored -> new ConcurrentHashMap<>()
        );
        Set<Vector2i> unregisteredRegions = new HashSet<>();
        for (Location location : plant.getStructure().locations()) {
            Vector3i pos = Vector3i.from(location);
            Vector2i regionPos = PlantRegion.toRegionCoords(pos);
            PlantRegion region = regions.get(regionPos);
            if (region == null) {
                continue;
            }
            region.unregister(pos);
            if (region.isEmpty()) {
                regions.remove(regionPos);
                unregisteredRegions.add(regionPos);
            }
        }
        return unregisteredRegions;
    }

    public void unregisterRegion(UUID worldUuid, Vector2i regionCoordinate) {
        Map<Vector2i, PlantRegion> regions = gardenPlants.get(worldUuid);
        if (regions == null) {
            return;
        }
        PlantRegion region = regions.get(regionCoordinate);
        if (region == null) {
            return;
        }
        for (GardenPlant gardenPlant : region.contents()) {
            boolean unloadCompletely = true;
            for (Location location : gardenPlant.getStructure().locations()) {
                Vector3i vector3i = Vector3i.from(location);
                Vector2i vector2i = PlantRegion.toRegionCoords(vector3i);
                if (!vector2i.equals(regionCoordinate) && regions.get(vector2i) != null) {
                    unloadCompletely = false;
                    break;
                }
            }
            if (unloadCompletely) {
                gardenPlantIds.remove(gardenPlant.getId());
            }
        }
        region.unload();
    }

    public void initializeWorld(UUID worldUuid, Collection<RegionData> regions) {
        gardenPlants.put(worldUuid, regions.stream()
                .map(regionData -> Map.entry(regionData.position(), new PlantRegion(regionData.regionId())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    public void unregisterWorld(@NonNull World world) {
        gardenPlants.remove(world.getUID());
        for (GardenPlant gardenPlant : List.copyOf(gardenPlantIds.values())) {
            if (gardenPlant.getStructure().origin().getWorld() == world) {
                gardenPlantIds.remove(gardenPlant.getId());
            }
        }
    }

    public Collection<GardenPlant> getPlants() {
        return gardenPlantIds.values();
    }

    public void clear() {
        gardenPlantIds.values().forEach(GardenPlant::clearBoundEntities);
        gardenPlants.clear();
        gardenPlantIds.clear();
    }

    public Optional<UUID> getRegionId(@NotNull UUID worldUuid, Vector2i regionPos) {
        return Optional.ofNullable(gardenPlants.get(worldUuid))
                .flatMap(regions -> Optional.ofNullable(regions.get(regionPos)))
                .map(PlantRegion::regionId);
    }
}
