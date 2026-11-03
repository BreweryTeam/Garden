package dev.jsinco.brewery.garden.tobacco;

import org.bukkit.util.Vector;

import java.util.List;

public record LineSmokeShape() implements SmokeShape {
    @Override
    public List<Vector> shapeAt(int smokeReleasePoint, Vector playerFacing) {
        return List.of(new Vector());
    }

    @Override
    public int smokeTicks() {
        return 10;
    }
}
