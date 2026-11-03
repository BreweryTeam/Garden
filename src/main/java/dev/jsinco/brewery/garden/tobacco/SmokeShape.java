package dev.jsinco.brewery.garden.tobacco;

import org.bukkit.util.Vector;

import java.util.List;

public interface SmokeShape {

    List<Vector> shapeAt(int smokeReleasePoint, Vector playerFacing);

    int smokeTicks();
}
