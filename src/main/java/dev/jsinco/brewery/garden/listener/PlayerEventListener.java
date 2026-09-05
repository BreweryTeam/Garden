package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.configuration.GardenConfig;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlacedFruitDisplays;
import dev.jsinco.brewery.garden.plant.PlantManager;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.plant.item.PlayerHeadBased;
import dev.jsinco.brewery.garden.registry.PlantRegistry;
import dev.jsinco.brewery.garden.structure.PlantStructure;
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
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerEventListener implements Listener {

    private final GardenConfig config;
    private static final Random RANDOM = new Random();
    public static final Set<BlockPlaceEvent> IGNORED_EVENTS = Collections.newSetFromMap(new ConcurrentHashMap<>());


    private final PlantRegistry gardenRegistry;
    private final PlantManager plantManager;

    public PlayerEventListener(PlantRegistry gardenRegistry, PlantManager plantManager) {
        this(gardenRegistry, GardenConfig.instance(), plantManager);
    }

    PlayerEventListener(PlantRegistry gardenRegistry, GardenConfig config, PlantManager plantManager) {
        this.gardenRegistry = gardenRegistry;
        this.config = config;
        this.plantManager = plantManager;
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || WorldUtil.isBlacklistedWorld(block.getLocation())) {
            return;
        }

        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();

        handlePlantShearing(event.getItem(), block, player);
        handleBonemeal(event, event.getItem(), block);
        if (event.getBlockFace() == BlockFace.UP && event.getAction().isRightClick() && config.plantableBlocks().contains(block.getType())) {
            event.setCancelled(handleSeedPlacement(player, hand, event.getItem(), block));
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
            gardenRegistry.getByID(uuid)
                    .ifPresent(GardenPlant::registerFruitPicked);
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

    private void handleBonemeal(PlayerInteractEvent event, ItemStack itemInHand, Block block) {
        if (itemInHand == null || itemInHand.getType() != Material.BONE_MEAL) {
            return;
        }
        if (!event.getAction().isRightClick()) {
            return;
        }
        GardenPlant plant = gardenRegistry.getByLocation(block).orElse(null);
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
                plant.incrementGrowthStage(1, gardenRegistry, plantManager));
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

    private boolean handleSeedPlacement(Player player, EquipmentSlot hand, ItemStack itemInHand, Block clickedBlock) {
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
        if (!checkBlocks(gardenPlant.getStructure(), itemInHand, clickedBlock, player, hand)) {
            return false;
        }
        plantManager.storeNewPlant(gardenPlant);

        itemInHand.setAmount(itemInHand.getAmount() - 1);
        location.getWorld().playSound(location, Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
        return true;
    }

    public boolean checkBlocks(PlantStructure structure, ItemStack itemInHand, Block clickedBlock, Player player, EquipmentSlot hand) {
        List<BlockState> previousStates = structure.pasteNow();
        if (previousStates.isEmpty()) {
            return false;
        }
        BlockMultiPlaceEvent event = new BlockMultiPlaceEvent(previousStates, clickedBlock, itemInHand, player, true, hand);
        IGNORED_EVENTS.add(event);
        if (!event.callEvent()) {
            previousStates.forEach(state -> state.update(true));
            IGNORED_EVENTS.remove(event);
            return false;
        }
        IGNORED_EVENTS.remove(event);
        return true;
    }
}
