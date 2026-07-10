package dev.jsinco.brewery.garden.plant;

import com.google.common.collect.ImmutableSet;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.plant.item.PlayerHeadBased;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString
public class GardenPlant {

    private final UUID id;
    private final PlantType type;
    private PlantStructure structure;
    private final String track;
    private int age;
    private static final Random RANDOM = new Random();
    private boolean bloomed = false;
    private final List<PlacedFruitDisplays> placedFruits = new ArrayList<>();
    private int expectedFruits;

    private static final Set<Material> DECORATIVE_PLANT_BLOCKS = compileDecorativePlantBlocks();

    private static Set<Material> compileDecorativePlantBlocks() {
        ImmutableSet.Builder<Material> builder = new ImmutableSet.Builder<>();
        builder.addAll(Tag.BUTTONS.getValues());
        builder.addAll(Tag.SLABS.getValues());
        builder.addAll(Tag.TRAPDOORS.getValues());
        builder.add(Material.MOSS_CARPET, Material.PINK_PETALS);
        return builder.build();
    }

    public GardenPlant(PlantType type, Location location) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.age = 0;
        List<String> tracks = type.structures().keySet().stream().toList();
        this.track = tracks.get(RANDOM.nextInt(tracks.size()));
        this.structure = type.newStructure(location, age, track);
        this.expectedFruits = 0;
    }

    public GardenPlant(UUID id, PlantType type, PlantStructure structure, String track, int age, int fruits) {
        this.id = id;
        this.type = type;
        this.age = age;
        this.track = track;
        this.structure = structure;
        this.expectedFruits = fruits;
        if (fruits > 0) {
            Bukkit.getGlobalRegionScheduler().run(Garden.getInstance(), t -> {
                placeFruits(fruits);
            });
        }
    }

    public boolean isFullyGrown() {
        return this.age >= type.structures().getOrDefault(track, List.of()).size() - 1;
    }

    public void incrementGrowthStage(int amount, PlantRegistry registry, GardenPlantDataType dataType) {
        this.setGrowthStage(age + amount, registry, dataType);
    }

    public void tick() {
        if (!structure.origin().isChunkLoaded()) {
            return;
        }
        int validCount = (int) placedFruits.stream()
                .filter(placedFruit -> !placedFruit.hasDepopulated())
                .count();
        int toPopulate = expectedFruits - validCount;
        placedFruits.removeIf(PlacedFruitDisplays::hasDepopulated);
        if (toPopulate > 0) {
            Bukkit.getRegionScheduler().run(Garden.getInstance(), structure.origin(), t -> {
                placedFruits.addAll(placeFruits(toPopulate));
            });
        }
    }

    public void setGrowthStage(int growthStage, PlantRegistry registry, GardenPlantDataType dataType) {
        if (!structure.origin().isChunkLoaded()) {
            return;
        }
        PlantStructure newStructure = type.newStructure(this.structure.origin(), growthStage, track);
        this.structure.remove().thenAccept(empty -> {
            if (!newStructure.locations(blockData -> !DECORATIVE_PLANT_BLOCKS.contains(blockData.getMaterial())).stream()
                    .map(Location::getBlock)
                    .map(Block::getType)
                    .allMatch(material -> material.isAir() || Tag.REPLACEABLE_BY_TREES.isTagged(material))
            ) {
                this.structure.paste();
                return;
            }
            this.age = growthStage;
            registry.unregisterPlant(this);
            newStructure.paste();
            this.structure = newStructure;
            registry.registerPlant(this);
            dataType.update(this);
        });
    }

    public void bloom() {
        if (!structure.origin().isChunkLoaded() || !placedFruits.isEmpty()) {
            return;
        }
        if (structure.locations(blockData -> Tag.LEAVES.isTagged(blockData.getMaterial()))
                .stream().map(Location::getBlock)
                .flatMap(block -> type.fruitPlacement().vectors().stream().map(block::getRelative))
                .anyMatch(block -> PlayerHeadBased.getPlantType(block) != null)
        ) {
            return;
        }
        List<Location> locationsRandomized = new ArrayList<>(
                this.structure.locations(blockData -> Tag.LEAVES.isTagged(blockData.getMaterial()))
        );
        Collections.shuffle(locationsRandomized);
        if (locationsRandomized.isEmpty()) {
            return;
        }
        int amount = RANDOM.nextInt(1, locationsRandomized.size() + 1);
        for (Location location : locationsRandomized) {
            Block block = location.getBlock();
            if (!Tag.LEAVES.isTagged(block.getType())) {
                continue;
            }
            if (amount-- <= 0) {
                break;
            }
            block.setBlockData(
                    BlockType.FLOWERING_AZALEA_LEAVES.createBlockData()
            );
            this.bloomed = true;
        }
    }

    public void placeFruits() {
        placedFruits.addAll(placeFruits(-1));
        expectedFruits = placedFruits.size();
        Garden.getInstance().getGardenPlantDataType().update(this);
    }

    private List<PlacedFruitDisplays> placeFruits(int amount) {
        if (!structure.origin().isChunkLoaded() || !type.bearFruits() || !placedFruits.isEmpty()) {
            return List.of();
        }
        PlantItem fruit = type.fruitItem();
        List<PlacedFruitDisplays> output = new ArrayList<>();
        int count = 0;
        for (Location location : this.structure.locations()) {
            if (amount != -1 && count >= amount) {
                return output;
            }
            Block block = location.getBlock();
            if (!Tag.LEAVES.isTagged(block.getType())) {
                continue;
            }
            if (amount == -1 && Material.FLOWERING_AZALEA_LEAVES != block.getType()) {
                continue;
            }
            List<BlockFace> relatives = type.fruitPlacement().vectors()
                    .stream()
                    .filter(relative -> block.getRelative(relative).getType().isAir())
                    .toList();
            if (relatives.isEmpty()) {
                continue;
            }
            BlockFace chosenRelative = relatives.get(RANDOM.nextInt(relatives.size()));
            fruit.place(block.getRelative(chosenRelative), chosenRelative, id, type)
                    .ifPresent(output::add);
            count++;
        }
        this.structure.paste(); // Clear all bloom blocks
        bloomed = false;
        return output;
    }

    public boolean hasBloomed() {
        return bloomed;
    }

    public boolean isAlive() {
        boolean hasLeaf = false;
        for (Location location : structure.locations()) {
            if (Tag.LEAVES.isTagged(location.getBlock().getType())) {
                hasLeaf = true;
            }
        }
        return hasLeaf && !structure.origin().getBlock().getType().isAir();
    }


    public Location origin() {
        return structure.origin();
    }

    public void registerFruitPicked() {
        for (int i = 0; i < placedFruits.size(); i++) {
            if (placedFruits.get(i).hasDepopulated()) {
                placedFruits.remove(i);
                break;
            }
        }
        expectedFruits--;
        if (expectedFruits < 0) {
            expectedFruits = 0;
        }
        Garden.getInstance().getGardenPlantDataType().update(this);
    }

    public void clearBoundEntities() {
        placedFruits.forEach(PlacedFruitDisplays::remove);
    }
}
