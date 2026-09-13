package dev.jsinco.brewery.garden.structure;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.configuration.GardenConfig;
import dev.thorinwasher.schem.Schematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.joml.Matrix3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
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

    public CompletableFuture<Void> paste() {
        CompletableFuture<Void> output = new CompletableFuture<>();
        Bukkit.getRegionScheduler().run(Garden.getInstance(), origin(), t -> {
            pasteNow();
            output.complete(null);
        });
        return output;
    }

    public List<BlockState> pasteNow() {
        World world = Bukkit.getWorld(worldUuid);
        if (world == null) {
            return List.of();
        }
        List<BlockState> output = new ArrayList<>();
        paste((vector, blockData) -> {
            Location location = new Location(world, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            output.add(location.getBlock().getState());
            world.setBlockData(location, blockData);
            if (GardenConfig.instance().overrideDrops()) {
                Garden.getInstance().getBlockUtil().disableItemDrops(world.getBlockAt(location));
            }
        }, vector -> world.getBlockData(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()));
        return output;
    }

    public void paste(BiConsumer<BlockVector, BlockData> pasteOperation, Function<BlockVector, BlockData> lookup) {
        Map<BlockVector, BlockData> mapped = new HashMap<>();
        Set<BlockVector> ignored = new HashSet<>();
        schematic.apply(transformation, (vector3i, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            if (blockData instanceof Leaves leaves) {
                leaves.setPersistent(false);
            }
            vector3i.sub(offset);
            BlockVector posToReplace = new BlockVector(originX + vector3i.x, originY + vector3i.y, originZ + vector3i.z);

            BlockData toReplace = lookup.apply(posToReplace);
            Material blockToReplaceType = toReplace.getMaterial();
            if (!Tag.REPLACEABLE_BY_TREES.isTagged(blockToReplaceType) && !blockToReplaceType.isAir()) {
                return;
            }
            if (vector3i.y() == 0) {
                if (!lookup.apply(posToReplace.clone().add(new Vector(0, -1, 0)).toBlockVector()).getMaterial().isSolid()) {
                    ignored.add(posToReplace);
                }
            }
            mapped.put(posToReplace, blockData);
        });
        Queue<BlockVector> toIgnore = new LinkedList<>(ignored);
        while (!toIgnore.isEmpty()) {
            BlockVector toCheck = toIgnore.poll();
            mapped.remove(toCheck);
            List<BlockVector> possiblyIgnore = new ArrayList<>();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockVector cloned = toCheck.clone();
                    cloned.add(new Vector(x, 1, z));
                    if (mapped.containsKey(cloned)) {
                        possiblyIgnore.add(cloned);
                    }
                }
            }
            for (BlockVector maybeIgnore : possiblyIgnore) {
                boolean found = false;
                for (int x = -2; x <= 2; x++) {
                    if (found) {
                        break;
                    }
                    for (int z = -2; z <= 2; z++) {
                        BlockVector cloned = maybeIgnore.clone();
                        cloned.add(new Vector(x, -1, z));
                        if (!ignored.contains(cloned) &&
                                mapped.get(cloned) instanceof BlockData blockData &&
                                blockData.getMaterial().isSolid()
                        ) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    toIgnore.add(maybeIgnore);
                    ignored.add(maybeIgnore);
                }
            }
        }
        mapped.forEach(pasteOperation);
    }

    public CompletableFuture<Void> remove() {
        World world = Bukkit.getWorld(worldUuid);
        if (world == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> output = new CompletableFuture<>();
        Bukkit.getRegionScheduler().run(Garden.getInstance(), origin(), ignored -> {
            schematic.apply(transformation, (vector3i, blockData) -> {
                if (blockData.getMaterial().isAir()) {
                    return;
                }
                vector3i.sub(offset);
                Location location = new Location(world, originX, originY, originZ).add(vector3i.x, vector3i.y, vector3i.z);

                if (location.getBlock().getType() == blockData.getMaterial()) {
                    location.getBlock().setType(Material.AIR);
                }
                if (GardenConfig.instance().overrideDrops()) {
                    Garden.getInstance().getBlockUtil().enableItemDrops(world.getBlockAt(location));
                }
            });
            output.complete(null);
        });
        return output;
    }

    public Location origin() {
        return new Location(Bukkit.getWorld(worldUuid), originX, originY, originZ);
    }
}
