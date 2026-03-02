package dev.jsinco.brewery.garden.utility;

import dev.jsinco.brewery.garden.Garden;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SchedulerUtil {

    private SchedulerUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void inRegion(Location location, Runnable task) {
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            task.run();
            return;
        }
        Bukkit.getRegionScheduler().run(Garden.getInstance(), location, t -> task.run());
    }
}
