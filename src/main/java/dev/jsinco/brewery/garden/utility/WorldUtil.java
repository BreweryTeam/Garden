package dev.jsinco.brewery.garden.utility;

import dev.jsinco.brewery.garden.Garden;
import org.bukkit.Location;

public class WorldUtil {


    public static boolean isBlacklistedWorld(Location location) {
        return Garden.getInstance().getPluginConfiguration().getBlacklistedWorlds().contains(location.getWorld().getName());
    }
}
