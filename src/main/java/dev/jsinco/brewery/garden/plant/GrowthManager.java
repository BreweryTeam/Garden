package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;

import java.util.*;

public class GrowthManager {

    private final PlantRegistry registry;
    private final Map<UUID, Long> growths = new HashMap<>();
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
                if (plant.hasBloomed()) {
                    plant.placeFruits();
                } else {
                    plant.bloom();
                }
                continue;
            }
            double probability = 1 - Math.pow(0.5, (double) 200 / plant.getType().growthTime());
            if (RANDOM.nextDouble() < probability) {
                plant.incrementGrowthStage(1, registry, dataType);
            }
        }
    }
}
