package dev.jsinco.brewery.garden.structure;

import dev.jsinco.brewery.garden.Garden;
import dev.thorinwasher.schem.Schematic;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.joml.Matrix3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public record PlantStructure(Schematic schematic, int originX, int originY, int originZ,
                             Matrix3d transformation, UUID worldUuid, Vector3i offset) {

    public List<Location> locations() {
        List<Location> locations = new ArrayList<>();
        World world = Bukkit.getWorld(worldUuid);
        schematic.apply(transformation, (vector3i, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            vector3i.sub(offset);
            locations.add(new Location(world, originX, originY, originZ).add(vector3i.x, vector3i.y, vector3i.z));
        });
        return locations;
    }

    public List<Location> locations(Predicate<BlockData> filter) {
        List<Location> locations = new ArrayList<>();
        World world = Bukkit.getWorld(worldUuid);
        schematic.apply(transformation, (vector3i, blockData) -> {
            if (blockData.getMaterial().isAir() || !filter.test(blockData)) {
                return;
            }
            vector3i.sub(offset);
            locations.add(new Location(world, originX, originY, originZ).add(vector3i.x, vector3i.y, vector3i.z));
        });
        return locations;
    }

    public void paste() {
        World world = Bukkit.getWorld(worldUuid);
        schematic.apply(transformation, (vector3i, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            if (blockData instanceof Leaves leaves) {
                leaves.setPersistent(false);
            }
            vector3i.sub(offset);
            Location posToReplace = new Location(world, originX, originY, originZ).add(vector3i.x, vector3i.y, vector3i.z);
            Material blockToReplaceType = posToReplace.getBlock().getType();
            if (!Tag.REPLACEABLE_BY_TREES.isTagged(blockToReplaceType) && !blockToReplaceType.isAir()) {
                return;
            }
            world.setBlockData(posToReplace, blockData);
            Garden.getInstance().getBlockUtil().disableItemDrops(world.getBlockAt(posToReplace));
        });
    }

    public void remove() {
        World world = Bukkit.getWorld(worldUuid);
        schematic.apply(transformation, (vector3i, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            vector3i.sub(offset);
            Location location = new Location(world, originX, originY, originZ).add(vector3i.x, vector3i.y, vector3i.z);
            if (location.getBlock().getType() == blockData.getMaterial()) {
                location.getBlock().setType(Material.AIR);
            }
            Garden.getInstance().getBlockUtil().enableItemDrops(world.getBlockAt(location));
        });
    }

    public Location origin() {
        return new Location(Bukkit.getWorld(worldUuid), originX, originY, originZ);
    }
}
