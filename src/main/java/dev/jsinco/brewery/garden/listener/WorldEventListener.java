package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.persist.PlantPdcType;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Arrays;
import java.util.List;

public record WorldEventListener(PlantRegistry registry, GardenPlantDataType gardenPlantDataType) implements Listener {


    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        loadWorld(event.getWorld());
    }

    public void loadWorld(World world) {
        gardenPlantDataType.fetch(world)
                .thenAcceptAsync(gardenPlants -> {
                    for (GardenPlant gardenPlant : gardenPlants) {
                        Bukkit.getRegionScheduler().run(Garden.getInstance(), gardenPlant.origin(), t -> {
                            registry.registerPlant(gardenPlant);
                        });
                    }
                });
        Arrays.stream(world.getLoadedChunks())
                .forEach(this::loadChunk);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        registry.unregisterWorld(event.getWorld());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        loadChunk(event.getChunk());
    }

    private void loadChunk(Chunk chunk) {
        List<GardenPlant> plants;
        try {
            plants = chunk
                    .getPersistentDataContainer()
                    .get(PlantPdcType.PLANT_KEY, PlantPdcType.PLANT_LIST_TYPE);
        } catch (IllegalArgumentException e) {
            plants = null;
        }
        if (plants == null) {
            return;
        }
        plants.forEach(registry::registerPlant);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        List<GardenPlant> plants;
        try {
            plants = event.getChunk()
                    .getPersistentDataContainer()
                    .get(PlantPdcType.PLANT_KEY, PlantPdcType.PLANT_LIST_TYPE);
        } catch (IllegalArgumentException e) {
            plants = null;
        }
        if (plants == null) {
            return;
        }
        plants.stream()
                .map(GardenPlant::getId)
                .forEach(registry::unregister);
    }

}
