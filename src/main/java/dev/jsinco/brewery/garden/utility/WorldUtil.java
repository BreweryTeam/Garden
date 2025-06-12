package dev.jsinco.brewery.garden.utility;

import dev.jsinco.brewery.garden.BreweryGarden;
import org.bukkit.Location;

public class WorldUtil {


    public static boolean isBlacklistedWorld(Location location) {
        return BreweryGarden.getInstance().getPluginConfiguration().getBlacklistedWorlds().contains(location.getWorld().getName());
    }
}
