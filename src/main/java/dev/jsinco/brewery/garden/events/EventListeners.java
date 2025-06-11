package dev.jsinco.brewery.garden.events;

import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.Seeds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class EventListeners implements Listener {

    private static final Random RANDOM = new Random();


    private final BreweryGardenConfig config = BreweryGarden.getInstance().getPluginConfiguration();
    private final PlantRegistry gardenRegistry;
    private final GardenPlantDataType gardenPlantDataType;

    public EventListeners(PlantRegistry gardenRegistry, GardenPlantDataType gardenPlantDataType) {
        this.gardenRegistry = gardenRegistry;
        this.gardenPlantDataType = gardenPlantDataType;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onLeafDecay(LeavesDecayEvent event) {
        if (isBlacklistedWorld(event.getBlock().getLocation())) {
            return;
        }
        GardenPlant gardenPlant = gardenRegistry.getByLocation(event.getBlock());
        if (gardenPlant != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isBlacklistedWorld(event.getBlock().getLocation())) {
            return;
        }
        Block block = event.getBlock();
        if (config.getValidSeedDropBlocks().contains(block.getType()) && RANDOM.nextInt(100) <= config.getSeedSpawnChance()) {
            // TODO drop seed
        }
        // TODO: add unregistration of structure
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || isBlacklistedWorld(block.getLocation())) {
            return;
        }

        handlePlantShearing(event.getItem(), block);
        if (event.getBlockFace() == BlockFace.UP && event.getAction().isRightClick() && config.getPlantableBlocks().contains(block.getType())) {
            event.setCancelled(handleSeedPlacement(event.getItem(), block));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isBlacklistedWorld(event.getBlock().getLocation())) {
            return;
        }
        if (Seeds.isSeeds(event.getItemInHand())) {
            PlantType plantType = Seeds.getSeeds(event.getItemInHand()).plantType();
            GardenPlant gardenPlant = new GardenPlant(
                    plantType,
                    event.getBlock().getLocation()
            );
            gardenPlant.getStructure().paste();
            gardenRegistry.registerPlant(gardenPlant);
            gardenPlantDataType.insert(gardenPlant);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        List<GardenPlant> gardenPlants = gardenPlantDataType.fetch(event.getWorld());
        gardenPlants.forEach(gardenRegistry::registerPlant);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        gardenRegistry.unregisterWorld(event.getWorld());
    }

    private void handlePlantShearing(ItemStack itemInHand, Block clickedBlock) {
        if (itemInHand == null || itemInHand.getType() != Material.SHEARS) {
            return;
        }

        Location clickedLocation = clickedBlock.getLocation();
        
        clickedLocation.getWorld().playSound(clickedLocation, Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
    }

    private boolean handleSeedPlacement(ItemStack itemInHand, Block clickedBlock) {
        if (!Seeds.isSeeds(itemInHand)) {
            return false;
        }

        Location location = clickedBlock.getLocation().add(0, 1, 0); // Need the block above

        Seeds seeds = Seeds.getSeeds(itemInHand);
        if (seeds == null) {
            return false;
        }
        // Create a new GardenPlant at the location
        GardenPlant gardenPlant = new GardenPlant(seeds.plantType(), location);
        gardenRegistry.registerPlant(gardenPlant);
        gardenPlantDataType.insert(gardenPlant);
        gardenPlant.getStructure().paste();

        itemInHand.setAmount(itemInHand.getAmount() - 1);
        location.getWorld().playSound(location, Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
        return true;
    }


    private boolean isBlacklistedWorld(Location location) {
        return config.getBlacklistedWorlds().contains(location.getWorld().getName());
    }
}
