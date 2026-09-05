package dev.jsinco.brewery.garden.utility.vector;

import org.bukkit.Location;
import org.bukkit.block.Block;

public record Vector3i(int x, int y, int z) {

    public static Vector3i from(Location location) {
        return new Vector3i(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public static Vector3i from(Block block) {
        return new Vector3i(
                block.getX(),
                block.getY(),
                block.getZ()
        );
    }
}
