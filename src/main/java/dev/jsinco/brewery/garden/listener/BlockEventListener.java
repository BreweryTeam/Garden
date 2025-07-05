package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.GardenRegistry;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.plant.Fruit;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.Seeds;
import dev.jsinco.brewery.garden.utility.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Skull;
import org.bukkit.block.data.type.WallSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class BlockEventListener implements Listener {
    private final PlantRegistry gardenRegistry;
    private static final Random RANDOM = new Random();
    private final GardenPlantDataType gardenPlantDataType;
    private BreweryGardenConfig config = Garden.getInstance().getPluginConfiguration();
    private static final List<BlockFace> FRUIT_FACES = List.of(BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST);

    public BlockEventListener(PlantRegistry gardenRegistry, GardenPlantDataType gardenPlantDataType) {
        this.gardenRegistry = gardenRegistry;
        this.gardenPlantDataType = gardenPlantDataType;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onLeafDecay(LeavesDecayEvent event) {
        if (WorldUtil.isBlacklistedWorld(event.getBlock().getLocation())) {
            return;
        }
        GardenPlant gardenPlant = gardenRegistry.getByLocation(event.getBlock());
        if (gardenPlant != null) {
            event.setCancelled(true);
        }
        checkSurroundingFruits(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (WorldUtil.isBlacklistedWorld(event.getBlock().getLocation())) {
            return;
        }
        Block block = event.getBlock();
        if (config.getValidSeedDropBlocks().contains(block.getType()) && RANDOM.nextInt(100) <= config.getSeedSpawnChance()) {
            List<PlantType> types = List.copyOf(GardenRegistry.PLANT_TYPE.values());
            PlantType chosen = types.get(RANDOM.nextInt(types.size()));
            ItemStack seeds = chosen.newSeeds().newItem(1);
            block.getWorld().dropItem(block.getLocation().toCenterLocation(), seeds);
        }
        checkFruit(block);
        GardenPlant gardenPlant = gardenRegistry.getByLocation(block);
        if (gardenPlant == null) {
            return;
        }
        checkAlive(gardenPlant);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        onMultiBlockDestroy(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        onMultiBlockDestroy(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        onMultiBlockDestroy(event.getBlocks());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        onMultiBlockDestroy(event.getBlocks());
    }

    private void onMultiBlockDestroy(Collection<Block> blocks) {
        Set<GardenPlant> plants = blocks.stream()
                .map(gardenRegistry::getByLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        plants.forEach(this::checkAlive);
        blocks.forEach(this::checkFruit);
    }

    private void checkAlive(GardenPlant gardenPlant) {
        Bukkit.getScheduler().runTask(Garden.getInstance(), () -> {
            if (gardenPlant.isAlive()) {
                return;
            }
            gardenRegistry.unregisterPlant(gardenPlant);
            gardenPlantDataType.remove(gardenPlant);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (Fruit.isFruit(event.getItemInHand())) {
            event.setCancelled(true);
            return;
        }
        if (WorldUtil.isBlacklistedWorld(event.getBlock().getLocation())) {
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

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (Garden.getGardenRegistry().getByLocation(event.getLocation().getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    private void checkFruit(Block block) {
        if (Fruit.getPlantType(block) == null) {
            return;
        }
        block.setType(Material.AIR);
    }

    private void checkSurroundingFruits(Block block) {
        for (BlockFace blockFace : FRUIT_FACES) {
            Block possibleFruit = block.getRelative(blockFace);
            PlantType plantType = Fruit.getPlantType(possibleFruit);
            if (plantType == null) {
                continue;
            }
            if (possibleFruit.getBlockData() instanceof Skull && blockFace != BlockFace.UP) {
                continue;
            }
            if (possibleFruit.getBlockData() instanceof WallSkull wallSkull && blockFace != wallSkull.getFacing()) {
                continue;
            }
            possibleFruit.setType(Material.AIR);
            possibleFruit.getWorld().dropItem(possibleFruit.getLocation().toCenterLocation(), plantType.newFruit().newItem(1));
        }
    }
}
