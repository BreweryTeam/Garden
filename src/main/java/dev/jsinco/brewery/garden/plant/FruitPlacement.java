package dev.jsinco.brewery.garden.plant;

import org.bukkit.block.BlockFace;

import java.util.List;

public enum FruitPlacement {
    ABOVE(BlockFace.UP),
    ADJACENT(BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH);

    private final List<BlockFace> vectors;

    FruitPlacement(BlockFace... vectors) {
        this.vectors = List.of(vectors);
    }

    public List<BlockFace> vectors() {
        return vectors;
    }
}
