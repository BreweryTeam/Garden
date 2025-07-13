package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.plant.Fruit;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.Seeds;
import dev.jsinco.brewery.garden.utility.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

public class EventListeners implements Listener {

    private BreweryGardenConfig config = Garden.getInstance().getPluginConfiguration();


    private final PlantRegistry gardenRegistry;
    private final GardenPlantDataType gardenPlantDataType;

    public EventListeners(PlantRegistry gardenRegistry, GardenPlantDataType gardenPlantDataType) {
        this.gardenRegistry = gardenRegistry;
        this.gardenPlantDataType = gardenPlantDataType;
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || WorldUtil.isBlacklistedWorld(block.getLocation())) {
            return;
        }

        handlePlantShearing(event.getItem(), block);
        if (event.getBlockFace() == BlockFace.UP && event.getAction().isRightClick() && config.getPlantableBlocks().contains(block.getType())) {
            event.setCancelled(handleSeedPlacement(event.getItem(), block));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPlace(BlockPlaceEvent event) {
        if (Seeds.isSeeds(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        gardenPlantDataType.fetch(event.getWorld())
                .thenAcceptAsync(gardenPlants ->
                        Bukkit.getGlobalRegionScheduler().run(Garden.getInstance(), task -> gardenPlants.forEach(gardenRegistry::registerPlant)));
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        gardenRegistry.unregisterWorld(event.getWorld());
    }

    private void handlePlantShearing(ItemStack itemInHand, Block clickedBlock) {
        if (itemInHand == null || itemInHand.getType() != Material.SHEARS) {
            return;
        }
        PlantType plantType = Fruit.getPlantType(clickedBlock);
        if (plantType == null) {
            return;
        }
        clickedBlock.setType(Material.AIR);
        clickedBlock.getWorld().dropItem(clickedBlock.getLocation().toCenterLocation(), plantType.newFruit().newItem(1));
    }

    private boolean handleSeedPlacement(ItemStack itemInHand, Block clickedBlock) {
        if (itemInHand == null || !Seeds.isSeeds(itemInHand)) {
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
}
