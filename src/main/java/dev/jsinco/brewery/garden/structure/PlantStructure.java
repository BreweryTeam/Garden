package dev.jsinco.brewery.garden.structure;

import dev.thorinwasher.schem.Schematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.joml.Matrix3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void paste() {
        World world = Bukkit.getWorld(worldUuid);
        schematic.apply(transformation, (vector3i, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            vector3i.sub(offset);
            world.setBlockData(new Location(world, originX, originY, originZ).add(vector3i.x, vector3i.y, vector3i.z), blockData);
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
        });
    }

    public Location origin() {
        return new Location(Bukkit.getWorld(worldUuid), originX, originY, originZ);
    }
}
