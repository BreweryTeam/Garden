package dev.jsinco.brewery.garden;

import dev.jsinco.brewery.garden.objects.GardenPlant;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GardenRegistry {
    @Getter
    private final List<GardenPlant> gardenPlants = new ArrayList<>();
    private final Map<UUID, GardenPlant> gardenPlantIds = new HashMap<>();


    @Nullable
    public GardenPlant getByID(UUID id) {
        return gardenPlantIds.get(id);
    }

    @Nullable
    public GardenPlant getByLocation(Block l) {
        if (l == null) return null;
        for (GardenPlant plant : gardenPlants) {
            if (plant.getRegion().getBlocks().contains(l)) {
                return plant;
            }
        }
        return null;
    }

    public void registerPlant(GardenPlant plant) {
        plant.place();
        gardenPlants.add(plant);
        gardenPlantIds.put(plant.getId(), plant);
    }

    public void unregisterPlant(GardenPlant plant) {
        plant.unPlace();
        gardenPlants.remove(plant);
        gardenPlantIds.remove(plant.getId());
    }
}
