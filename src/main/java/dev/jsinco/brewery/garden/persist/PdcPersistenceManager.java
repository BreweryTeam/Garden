package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.configuration.GardenConfig;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record PdcPersistenceManager(GardenPlantDataType dataType) implements PersistenceManager {
    @Override
    public CompletableFuture<Void> update(GardenPlant gardenPlant) {
        StorageSolution currentStorage = gardenPlant.getCurrentStorage();
        if (GardenConfig.instance().storageSolution() == StorageSolution.SQLITE && currentStorage != StorageSolution.PDC) {
            dataType.update(gardenPlant);
        } else {
            if (currentStorage == StorageSolution.SQLITE) {
                dataType.remove(gardenPlant);
                gardenPlant.setCurrentStorage(StorageSolution.PDC);
            }
            Chunk chunk = gardenPlant.origin().getChunk();
            List<GardenPlant> plants;
            try {
                plants = chunk
                        .getPersistentDataContainer()
                        .get(PlantPdcType.PLANT_KEY, PlantPdcType.PLANT_LIST_TYPE);
            } catch (IllegalArgumentException e) {
                plants = null;
            }
            List<GardenPlant> gardenPlants;
            if (plants == null) {
                gardenPlants = new ArrayList<>();
            } else {
                gardenPlants = new ArrayList<>(plants);
            }
            gardenPlants.removeIf(plant -> plant.getId().equals(gardenPlant.getId()));
            gardenPlants.add(gardenPlant);
            chunk.getPersistentDataContainer().set(PlantPdcType.PLANT_KEY, PlantPdcType.PLANT_LIST_TYPE, gardenPlants);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> remove(GardenPlant gardenPlant) {
        Chunk chunk = gardenPlant.getStructure().origin().getChunk();
        List<GardenPlant> plants;
        try {
            plants = chunk
                    .getPersistentDataContainer()
                    .get(PlantPdcType.PLANT_KEY, PlantPdcType.PLANT_LIST_TYPE);
        } catch (IllegalArgumentException e) {
            plants = null;
        }
        List<GardenPlant> gardenPlants;
        if (plants == null) {
            return CompletableFuture.completedFuture(null);
        } else {
            gardenPlants = new ArrayList<>(plants);
        }
        gardenPlants.removeIf(plant -> plant.getId().equals(gardenPlant.getId()));
        if (!gardenPlants.isEmpty()) {
            chunk.getPersistentDataContainer().set(PlantPdcType.PLANT_KEY, PlantPdcType.PLANT_LIST_TYPE, gardenPlants);
        } else {
            chunk.getPersistentDataContainer().remove(PlantPdcType.PLANT_KEY);
        }
        return CompletableFuture.completedFuture(null);
    }
}
