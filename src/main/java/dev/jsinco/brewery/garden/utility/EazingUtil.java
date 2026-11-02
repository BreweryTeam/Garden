package dev.jsinco.brewery.garden.utility;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class EazingUtil {

    private EazingUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static double lerp(double progress, double from, double to) {
        return (1 - progress) * from + progress * to;
    }

    public static Vector lerp(double progress, Vector from, Vector to) {
        return from.clone().multiply(1 - progress)
                .add(to.clone().multiply(progress));
    }

    public static Location lerp(double progress, Location from, Location to) {
        return new Location(
                to.getWorld(),
                lerp(progress, from.getX(), to.getX()),
                lerp(progress, from.getY(), to.getY()),
                lerp(progress, from.getZ(), to.getZ()),
                (float) lerpMod(progress, from.getYaw(), to.getYaw(), 360),
                (float) lerpMod(progress, from.getPitch(), to.getPitch(), 360)
        );
    }

    public static double lerpMod(double progress, double angleFrom, double angleTo, double mod) {
        double angleToMod = Math.abs(angleTo - angleFrom) > Math.abs(angleTo + mod - angleFrom) ?
                angleTo + mod : angleTo;
        return lerp(progress, angleFrom, angleToMod);
    }

    public static double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }
}
