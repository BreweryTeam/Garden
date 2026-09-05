package dev.jsinco.brewery.garden.utility.vector;

import org.bukkit.Location;
import org.bukkit.block.Block;

public record Vector2i(int x, int z) {

    public Vector2i from(Location location) {
        return new Vector2i(location.getBlockX(), location.getBlockZ());
    }

    public Vector2i from(Block block) {
        return new Vector2i(block.getX(), block.getZ());
    }

    public Vector2i from(Vector3i vector3i) {
        return new Vector2i(vector3i.x(), vector3i.z());
    }

}
