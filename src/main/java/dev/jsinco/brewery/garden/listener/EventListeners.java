package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.configuration.GardenConfig;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlacedFruitDisplays;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.plant.item.PlayerHeadBased;
import dev.jsinco.brewery.garden.utility.Encoder;
import dev.jsinco.brewery.garden.utility.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EventListeners implements Listener {

    private final GardenConfig config = GardenConfig.instance();
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
        if (event.getBlockFace() == BlockFace.UP && event.getAction().isRightClick() && config.plantableBlocks().contains(block.getType())) {
            event.setCancelled(handleSeedPlacement(event.getItem(), block));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        PersistentDataContainer pdc = event.getRightClicked().getPersistentDataContainer();
        List<byte[]> boundEntities = pdc
                .get(PlacedFruitDisplays.INTERACTION_ENTITY_DATA, PersistentDataType.LIST.listTypeFrom(PersistentDataType.BYTE_ARRAY));
        if (boundEntities == null) {
            return;
        }
        ItemStack hand = event.getPlayer().getInventory().getItem(event.getHand());
        if (hand.getType() != Material.SHEARS) {
            return;
        }
        World world = event.getPlayer().getWorld();
        for (byte[] boundEntity : boundEntities) {
            Entity entity = world.getEntity(Encoder.asUuid(boundEntity));
            if (entity != null) {
                entity.remove();
            }
        }
        byte[] owningPlant = pdc.get(PlacedFruitDisplays.OWNING_PLANT, PersistentDataType.BYTE_ARRAY);
        if (owningPlant != null) {
            UUID uuid = Encoder.asUuid(owningPlant);
            GardenPlant gardenPlant = gardenRegistry.getByID(uuid);
            if (gardenPlant != null) {
                gardenPlant.registerFruitPicked();
            }
        }
        event.getRightClicked().remove();
        String plantTypeString = pdc.get(PlantItem.PLANT_TYPE_KEY, PersistentDataType.STRING);
        if (plantTypeString == null) {
            return;
        }
        NamespacedKey key = NamespacedKey.fromString(plantTypeString);
        if (key == null) {
            return;
        }
        PlantType plantType = MutableGardenRegistry.PLANT_TYPE.get(key);
        if (plantType == null) {
            return;
        }
        plantType.fruitItem().item(PlantItem.PlantItemType.FRUIT, plantType)
                .ifPresent(item -> world.dropItem(
                        event.getRightClicked().getLocation(),
                        item
                ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPlace(BlockPlaceEvent event) {
        if (PlantItem.plantItemKey(event.getItemInHand()) != null) {
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
        if (!config.bonemealGrowth() || plant.isFullyGrown()) {
            return;
        }
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        }
        plant.origin().getWorld().playEffect(plant.origin(), Effect.BONE_MEAL_USE, 5);
        if (RANDOM.nextInt(100) >= config.bonemealChance()) {
            return;
        }
        Bukkit.getRegionScheduler().run(Garden.getInstance(), plant.origin(), t ->
                plant.incrementGrowthStage(1, gardenRegistry, gardenPlantDataType));
    }

    private void handlePlantShearing(ItemStack itemInHand, Block clickedBlock, Player player) {
        if (itemInHand == null || itemInHand.getType() != Material.SHEARS) {
            return;
        }
        PlantType plantType = PlayerHeadBased.getPlantType(clickedBlock);
        if (plantType == null) {
            return;
        }
        if (player.getGameMode() != GameMode.CREATIVE) {
            itemInHand.damage(1, player);
        }
        clickedBlock.setType(Material.AIR);
        plantType.fruitItem().item(PlantItem.PlantItemType.FRUIT, plantType)
                .ifPresent(item -> clickedBlock.getWorld().dropItem(clickedBlock.getLocation().toCenterLocation(), item));
    }

    private boolean handleSeedPlacement(ItemStack itemInHand, Block clickedBlock) {
        if (itemInHand == null || !PlantItem.isSeeds(itemInHand)) {
            return false;
        }

        Location location = clickedBlock.getLocation().add(0, 1, 0); // Need the block above

        PlantType plantType = PlantItem.plantType(itemInHand);
        if (plantType == null) {
            return false;
        }
        // Create a new GardenPlant at the location
        GardenPlant gardenPlant = new GardenPlant(plantType, location);
        gardenRegistry.registerPlant(gardenPlant);
        gardenPlantDataType.insert(gardenPlant);
        gardenPlant.getStructure().paste();

        itemInHand.setAmount(itemInHand.getAmount() - 1);
        location.getWorld().playSound(location, Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
        return true;
    }
}
