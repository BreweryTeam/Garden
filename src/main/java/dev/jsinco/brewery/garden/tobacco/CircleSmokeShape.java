package dev.jsinco.brewery.garden.tobacco;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public record CircleSmokeShape(double radius, int smokeTicks) implements SmokeShape {
    private static final int SMOKE_CIRCLE_POINTS = 16;

    @Override
    public List<Vector> shapeAt(int smokeReleasePoint, Vector playerFacing) {
        Vector xDirection = playerFacing.clone().crossProduct(new Vector(0, 1, 0))
                .normalize();
        Vector zDirection = xDirection.clone().crossProduct(playerFacing);
        Random random = ThreadLocalRandom.current();
        double offsetRadians = random.nextDouble(Math.PI * 2);
        List<Vector> output = new ArrayList<>();
        for (int i = 0; i < SMOKE_CIRCLE_POINTS; i++) {
            double radians = Math.PI * 2 * i / SMOKE_CIRCLE_POINTS;
            output.add(xDirection.clone()
                    .multiply(Math.cos(offsetRadians + radians))
                    .add(zDirection.clone()
                            .multiply(Math.sin(offsetRadians + radians))
                    ).multiply(radius)
            );
        }
        return output;
    }
}
