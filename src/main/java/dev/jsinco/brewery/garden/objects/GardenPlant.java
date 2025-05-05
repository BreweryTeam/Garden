package dev.jsinco.brewery.garden.objects;

import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.constants.PlantPart;
import dev.jsinco.brewery.garden.constants.PlantType;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class GardenPlant {

    // Yes, yes. I know, schematics would be the best way to do this.
    // If you're reading this, maybe submit a PR? ^ :)

    private static final Random RANDOM = new Random();
    private static final BreweryGardenConfig config = BreweryGarden.getInstance().getPluginConfiguration();

    private final UUID id;
    private final PlantType type;
    private final WorldTiedBoundingBox region;
    private int age;

    public GardenPlant(PlantType type, Location bottomLocation) {
        this(type, WorldTiedBoundingBox.of(bottomLocation, bottomLocation.clone().add(0, 2, 0)));
    }

    public GardenPlant(PlantType type, WorldTiedBoundingBox region) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.region = region;
        this.age = 0;
    }

    // Plants are:
    // BOTTOM = OAK_FENCE
    // MIDDLE = OAK_LEAVES
    // TOP = PLANT HEAD
    public boolean place() {
        for (var plantPart : PlantPart.values()) {
            if (plantPart == PlantPart.TOP) {
                if (!this.isFullyGrown()) {
                    this.age++;
                    continue;
                }
                this.age = 0;
            }

            Location location = plantPart.locationFromCenter(region.getCenter(), region.getWorld());
            Block block = location.getBlock();
            if (block.getType() != Material.AIR) {
                continue;
            }

            block.setType(plantPart.getAssigneeMaterial());
            if (block.getBlockData() instanceof Leaves leaves) {
                leaves.setPersistent(true);
                block.setBlockData(leaves);
            }
            if (plantPart.getAssigneeMaterial() == Material.PLAYER_HEAD) {
                type.setSkullTexture(block);
                block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().toCenterLocation(), 5, 0.1, 0.1, 0.1);
            }
        }
        return true;
    }

    @Nullable
    public PlantPart getPlantPart(Location location) {
        for (PlantPart part : PlantPart.values()) {
            Location locationFromCenter = part.locationFromCenter(region.getCenter(), region.getWorld());
            if (locationFromCenter.equals(location)) {
                return part;
            }
        }
        return null;
    }


    public boolean unPlace() {
        for (Block block : region.getBlocks()) {
            if (block.getType() == Material.AIR) {
                continue;
            } else if (block.getType() == Material.PLAYER_HEAD) {
                block.getWorld().dropItemNaturally(block.getLocation(), this.type.getItemStack(RANDOM.nextInt(1, 4)));
            }
            block.setType(Material.AIR);
        }
        return true;
    }


    public boolean isValid() {
        for (PlantPart part : PlantPart.values()) {
            if (part == PlantPart.TOP) {
                continue; // This could be air or a PlayerHead
            }

            Location location = part.locationFromCenter(region.getCenter(), region.getWorld());
            Block block = location.getBlock();
            if (block.getType() != part.getAssigneeMaterial()) {
                return false;
            }
        }
        return true;
    }


    public boolean isFullyGrown() {
        return this.age >= config.getFullyGrown();
    }

    public void incrementGrowthStage(int amount) {
        this.age += amount;
    }

    public void resetGrowthStage(boolean dropPlantItem) {
        this.age = 0;
        Location location = PlantPart.TOP.locationFromCenter(region.getCenter(), region.getWorld());
        location.getBlock().setType(Material.AIR);

        if (dropPlantItem) {
            // Drop between 1-3 of the ItemStack
            location.getWorld().dropItemNaturally(location, this.type.getItemStack(RANDOM.nextInt(1, 4)));
        }
    }
}
