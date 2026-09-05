package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.database.Database;
import dev.jsinco.brewery.garden.database.RegionSession;
import dev.jsinco.brewery.garden.registry.PlantRegion;
import dev.jsinco.brewery.garden.registry.PlantRegistry;
import dev.jsinco.brewery.garden.utility.vector.Vector2i;
import dev.jsinco.brewery.garden.utility.vector.Vector3i;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlantManager {

    private final PlantRegistry registry;
    private final Database database;

    public PlantManager(PlantRegistry registry, Database database) {
        this.registry = registry;
        this.database = database;
    }

    public void tick() {
        Random random = ThreadLocalRandom.current();
        for (GardenPlant plant : registry.getPlants()) {
            if (plant.isFullyGrown()) {
                if (random.nextDouble() > 1 - Math.pow(0.5, (double) 400 / plant.getType().growthTime())) {
                    continue;
                }
                Bukkit.getRegionScheduler().run(Garden.getInstance(), plant.origin(), t -> {
                    if (plant.hasBloomed()) {
                        plant.placeFruits();
                    } else {
                        plant.bloom();
                    }
                });
            } else {
                double probability = 1 - Math.pow(0.5, (double) 200 / plant.getType().growthTime());
                if (random.nextDouble() < probability) {
                    Bukkit.getRegionScheduler().run(Garden.getInstance(), plant.origin(), t -> {
                        plant.incrementGrowthStage(1, registry, this);
                    });
                }
            }
        }
    }

    private void storePlant(GardenPlant gardenPlant, Function<GardenPlant, CompletableFuture<Void>> storeOperation) {
        Set<Vector2i> regionCoords = gardenPlant.getStructure().locations()
                .stream()
                .map(Vector3i::from)
                .map(PlantRegion::toRegionCoords)
                .collect(Collectors.toSet());
        UUID worldUuid = gardenPlant.getStructure().worldUuid();
        List<Vector2i> newRegions = regionCoords.stream()
                .filter(regionCoordinate -> registry.getRegionId(worldUuid, regionCoordinate).isEmpty())
                .toList();
        CompletableFuture<Void> plantFuture = storeOperation.apply(gardenPlant);
        List<CompletableFuture<Void>> regionFutures = new ArrayList<>();
        registry.registerPlant(gardenPlant);
        RegionSession regionSession = database.regionSession();
        for (Vector2i newRegionPos : newRegions) {
            UUID regionId = registry.getRegionId(worldUuid, newRegionPos).orElse(null);
            if (regionId == null) {
                continue;
            }
            regionFutures.add(regionSession.insertRegion(worldUuid, newRegionPos, regionId));
        }
        CompletableFuture<Void> regionFuture = CompletableFuture.allOf(
                regionFutures.toArray(CompletableFuture<?>[]::new)
        );
        CompletableFuture.allOf(plantFuture, regionFuture)
                .thenAccept(ignored ->
                        regionCoords
                                .stream()
                                .map(regionPos -> registry.getRegionId(worldUuid, regionPos))
                                .flatMap(Optional::stream)
                                .forEach(regionId -> regionSession.insertPlantRegionRelation(regionId, gardenPlant.getId()))
                );
    }

    public void updatePlant(GardenPlant gardenPlant) {
        storePlant(gardenPlant, database.plantSession()::update);
    }

    public void storeNewPlant(GardenPlant gardenPlant) {
        storePlant(gardenPlant, database.plantSession()::insert);
    }

    public void removePlant(GardenPlant gardenPlant) {
        RegionSession regionSession = database.regionSession();
        registry.unregisterPlant(gardenPlant)
                .forEach(pos -> regionSession.removeRegion(gardenPlant.getStructure().worldUuid(), pos));
        database.plantSession().remove(gardenPlant);
    }

    public void loadWorld(UUID worldUuid) {
    }
}
