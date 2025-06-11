package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.BreweryGarden;
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

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
public class GardenPlant {
    private static final BreweryGardenConfig config = BreweryGarden.getInstance().getPluginConfiguration();

    private final UUID id;
    private final PlantType type;
    private PlantStructure structure;
    private final String track;
    private int age;
    private static final Random RANDOM = new Random();
    private boolean bloomed = false;

    public GardenPlant(PlantType type, Location location) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.age = 0;
        this.track = type.getRandomTrack();
        this.structure = type.newStructure(location, age, track);
    }

    public boolean isFullyGrown() {
        return this.age >= type.stages() - 1;
    }

    public void incrementGrowthStage(int amount, PlantRegistry registry, GardenPlantDataType dataType) {
        this.setGrowthStage(age + amount, registry, dataType);
    }

    public void setGrowthStage(int growthStage, PlantRegistry registry, GardenPlantDataType dataType) {
        this.age = growthStage;
        this.structure.remove();
        registry.unregisterPlant(this);
        this.structure = type.newStructure(this.structure.origin(), this.age, track);
        this.structure.paste();
        registry.registerPlant(this);
        dataType.update(this);
    }

    public void bloom() {
        // TODO don't load chunks
        for (Location location : this.structure.locations()) {
            Block block = location.getBlock();
            if (!Tag.LEAVES.isTagged(block.getType())) {
                continue;
            }
            if (RANDOM.nextBoolean()) {
                block.setBlockData(
                        BlockType.FLOWERING_AZALEA_LEAVES.createBlockData(blockData -> {
                            blockData.setPersistent(true);
                        })
                );
            }
        }
        this.bloomed = true;
    }

    public void placeFruits() {
        // TODO don't load chunks
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
}
