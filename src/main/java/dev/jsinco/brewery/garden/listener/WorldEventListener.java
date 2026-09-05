package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.database.Database;
import dev.jsinco.brewery.garden.database.GardenPlantSession;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.registry.PlantRegion;
import dev.jsinco.brewery.garden.registry.PlantRegistry;
import dev.jsinco.brewery.garden.utility.vector.Vector2i;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record WorldEventListener(Database database, PlantRegistry plantRegistry) implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        database.regionSession().allRegions(event.getWorld().getUID())
                .thenAcceptAsync(regionDataCollection -> {
                    plantRegistry.initializeWorld(event.getWorld().getUID(), regionDataCollection);
                });
        for (Chunk chunk : event.getWorld().getLoadedChunks()) {
            Vector2i regionPos = PlantRegion.toRegionCoords(chunk);
            loadRegion(regionPos, event.getWorld());
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        plantRegistry.unregisterWorld(event.getWorld());
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        Vector2i regionPos = PlantRegion.toRegionCoords(event.getChunk());
        loadRegion(regionPos, event.getWorld());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkUnloadEvent event) {
        plantRegistry.unregisterRegion(event.getWorld().getUID(), new Vector2i(event.getChunk().getX(), event.getChunk().getZ()));
    }

    private void loadRegion(Vector2i regionPos, World world) {
        Optional<UUID> regionId = plantRegistry.getRegionId(world.getUID(), regionPos);
        if (regionId.isEmpty()) {
            return;
        }
        GardenPlantSession plantSession = database.plantSession();
        database.regionSession().findRelatedPlants(regionId.get())
                .thenAccept(plantIds -> {
                            List<CompletableFuture<GardenPlant>> plants = plantIds.stream()
                                    .map(plantId -> plantRegistry.getByID(plantId)
                                            .map(CompletableFuture::completedFuture)
                                            .orElse(plantSession.findFromPlantId(world, plantId))
                                    ).toList();
                            CompletableFuture.allOf(plants.toArray(CompletableFuture[]::new)) // Join together to avoid concurrency issues
                                    .thenAccept(ignored -> plants.stream()
                                            .map(CompletableFuture::join)
                                            .forEach(plantRegistry::registerPlant)
                                    );
                        }
                );
    }
}
