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
import org.bukkit.*;
import org.bukkit.block.Block;
import java.util.Random;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
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
    private static final Random RANDOM = new Random();


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

        handlePlantShearing(event.getItem(), block, event.getPlayer());
        handleBonemeal(event, event.getItem(), block);
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
                .thenAcceptAsync(gardenPlants -> {
                    for (GardenPlant gardenPlant : gardenPlants) {
                        Bukkit.getRegionScheduler().run(Garden.getInstance(), gardenPlant.origin(), t -> {
                            gardenRegistry.registerPlant(gardenPlant);
                        });
                    }
                });
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        gardenRegistry.unregisterWorld(event.getWorld());
    }

    private void handleBonemeal(PlayerInteractEvent event, ItemStack itemInHand, Block block) {
        if (itemInHand == null || itemInHand.getType() != Material.BONE_MEAL) {
            return;
        }
        if (!event.getAction().isRightClick()) {
            return;
        }
        GardenPlant plant = gardenRegistry.getByLocation(block);
        if (plant == null) {
            return;
        }
        event.setCancelled(true);
        if (!config.isBonemealGrowth() || plant.isFullyGrown()) {
            return;
        }
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        }
        plant.origin().getWorld().playEffect(plant.origin(), Effect.BONE_MEAL_USE, 5);
        if (RANDOM.nextInt(100) >= config.getBonemealChance()) {
            return;
        }
        Bukkit.getRegionScheduler().run(Garden.getInstance(), plant.origin(), t ->
                plant.incrementGrowthStage(1, gardenRegistry, gardenPlantDataType));
    }

    private void handlePlantShearing(ItemStack itemInHand, Block clickedBlock, Player player) {
        if (itemInHand == null || itemInHand.getType() != Material.SHEARS) {
            return;
        }
        PlantType plantType = Fruit.getPlantType(clickedBlock);
        if (plantType == null) {
            return;
        }
        if (player.getGameMode() != GameMode.CREATIVE) {
            itemInHand.damage(1, player);
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
