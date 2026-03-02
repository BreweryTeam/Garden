package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import org.bukkit.Bukkit;

import java.util.*;

public class GrowthManager {

    private final PlantRegistry registry;
    private final static Random RANDOM = new Random();
    private final GardenPlantDataType dataType;

    public GrowthManager(PlantRegistry registry, GardenPlantDataType dataType) {
        this.registry = registry;
        this.dataType = dataType;
    }

    public void tick() {
        for (GardenPlant plant : List.copyOf(registry.getPlants())) {
            if (plant.isFullyGrown()) {
                if (RANDOM.nextDouble() > 1 - Math.pow(0.5, (double) 400 / plant.getType().growthTime())) {
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
                if (RANDOM.nextDouble() < probability) {
                    Bukkit.getRegionScheduler().run(Garden.getInstance(), plant.origin(), t -> {
                        plant.incrementGrowthStage(1, registry, dataType);
                    });
                }
            }
        }
    }
}
