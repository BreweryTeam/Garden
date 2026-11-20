package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.plant.GardenPlant;

import java.util.concurrent.CompletableFuture;

public interface PersistenceManager {

    CompletableFuture<Void> update(GardenPlant gardenPlant);

    CompletableFuture<Void> remove(GardenPlant gardenPlant);
}
