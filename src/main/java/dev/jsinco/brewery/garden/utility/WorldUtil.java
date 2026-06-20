package dev.jsinco.brewery.garden.utility;

import dev.jsinco.brewery.garden.configuration.GardenConfig;
import org.bukkit.Location;

public class WorldUtil {


    public static boolean isBlacklistedWorld(Location location) {
        return GardenConfig.instance().blacklistedWorlds().contains(location.getWorld().getName());
    }
}
