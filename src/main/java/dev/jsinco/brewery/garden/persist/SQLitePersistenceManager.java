package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.plant.GardenPlant;

import java.util.concurrent.CompletableFuture;

public record SQLitePersistenceManager(GardenPlantDataType dataType) implements PersistenceManager {
    @Override
    public CompletableFuture<Void> update(GardenPlant gardenPlant) {
        return dataType.update(gardenPlant);
    }

    @Override
    public CompletableFuture<Void> remove(GardenPlant gardenPlant) {
        return dataType.remove(gardenPlant);
    }
}
