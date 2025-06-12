package dev.jsinco.brewery.garden;

import dev.jsinco.brewery.garden.plant.GardenPlant;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlantRegistry {
    private final Map<UUID, Map<BlockVector, GardenPlant>> gardenPlants = new HashMap<>();
    private final Map<UUID, GardenPlant> gardenPlantIds = new HashMap<>();


    @Nullable
    public GardenPlant getByID(UUID id) {
        return gardenPlantIds.get(id);
    }

    @Nullable
    public GardenPlant getByLocation(Block block) {
        World world = block.getWorld();
        BlockVector position = block.getLocation().toVector().toBlockVector();
        return gardenPlants.computeIfAbsent(world.getUID(), ignored -> new HashMap<>()).get(position);
    }

    public void registerPlant(GardenPlant plant) {
        UUID worldUuid = plant.getStructure().worldUuid();
        System.out.println(plant.getStructure().transformation());
        System.out.println(plant.getStructure().offset());
        for (Location location : plant.getStructure().locations()) {
            gardenPlants.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).put(
                    location.toVector().toBlockVector(), plant
            );
            System.out.println(location);
        }
        gardenPlantIds.put(plant.getId(), plant);
    }

    public void unregisterPlant(GardenPlant plant) {
        gardenPlantIds.remove(plant.getId());
        UUID worldUuid = plant.getStructure().worldUuid();
        for (Location location : plant.getStructure().locations()) {
            gardenPlants.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).remove(
                    location.toVector().toBlockVector()
            );
        }
    }

    public void unregisterWorld(@NotNull World world) {
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
}
