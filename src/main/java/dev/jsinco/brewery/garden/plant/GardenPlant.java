package dev.jsinco.brewery.garden.plant;

import com.google.common.collect.ImmutableSet;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;

import java.util.*;

@Getter
@ToString
@AllArgsConstructor
public class GardenPlant {
    private static final BreweryGardenConfig config = Garden.getInstance().getPluginConfiguration();

    private final UUID id;
    private final PlantType type;
    private PlantStructure structure;
    private final String track;
    private int age;
    private static final Random RANDOM = new Random();
    private boolean bloomed = false;
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
        this.track = type.getRandomTrack();
        this.structure = type.newStructure(location, age, track);
        if (this.structure == null) {
            throw new IllegalStateException("Could not create plant structure for type " + type.displayName());
        }
    }

    public boolean isFullyGrown() {
        return this.age >= type.stages() - 1;
    }

    public void incrementGrowthStage(int amount, PlantRegistry registry, GardenPlantDataType dataType) {
        this.setGrowthStage(age + amount, registry, dataType);
    }

    /**
     * Attempts to set the growth stage of the plant to the specified value.
     * If the new structure cannot be placed (due to collisions or other reasons),
     * the growth stage remains unchanged
     * and the current age is returned.
     *
     * @param growthStage The desired growth stage to set.
     * @param registry The PlantRegistry to manage plant registrations.
     * @param dataType The GardenPlantDataType for updating plant data.
     * @return The actual growth stage after the operation.
     */
    public int setGrowthStage(int growthStage, PlantRegistry registry, GardenPlantDataType dataType) {
        if (!structure.origin().isChunkLoaded()) {
            return this.age;
        }
        this.structure.remove();
        PlantStructure newStructure = type.newStructure(this.structure.origin(), growthStage, track);
        if (newStructure == null || !newStructure.locations(blockData -> !DECORATIVE_PLANT_BLOCKS.contains(blockData.getMaterial())).stream()
                .map(Location::getBlock)
                .map(Block::getType)
                .allMatch(material -> material.isAir() || Tag.REPLACEABLE_BY_TREES.isTagged(material))
        ) {
            this.structure.paste();
            return this.age;
        }
        this.age = growthStage;
        registry.unregisterPlant(this);
        newStructure.paste();
        this.structure = newStructure;
        registry.registerPlant(this);
        dataType.update(this);
        return this.age;
    }

    public void bloom() {
        if (!structure.origin().isChunkLoaded()) {
            return;
        }
        if (structure.locations(blockData -> Tag.LEAVES.isTagged(blockData.getMaterial()))
                .stream().map(Location::getBlock)
                .flatMap(block -> type.fruitPlacement().vectors().stream().map(block::getRelative))
                .anyMatch(block -> Fruit.getPlantType(block) != null)
        ) {
            return;
        }
        for (Location location : this.structure.locations(blockData -> Tag.LEAVES.isTagged(blockData.getMaterial()))) {
            Block block = location.getBlock();
            if (!Tag.LEAVES.isTagged(block.getType())) {
                continue;
            }
            if (RANDOM.nextBoolean()) {
                block.setBlockData(
                        BlockType.FLOWERING_AZALEA_LEAVES.createBlockData()
                );
                this.bloomed = true;
            }
        }
    }

    public void placeFruits() {
        if (!structure.origin().isChunkLoaded()) {
            return;
        }
        Fruit fruit = type.newFruit();
        for (Location location : this.structure.locations()) {
            Block block = location.getBlock();
            if (Material.FLOWERING_AZALEA_LEAVES != block.getType()) {
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
            fruit.placeFruit(block.getRelative(chosenRelative), chosenRelative);
        }
        this.structure.paste(); // Clear all bloom blocks
        bloomed = false;
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
}
