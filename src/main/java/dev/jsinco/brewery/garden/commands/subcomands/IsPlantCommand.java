package dev.jsinco.brewery.garden.commands.subcomands;

import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.GardenRegistry;
import dev.jsinco.brewery.garden.commands.AddonSubCommand;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.objects.GardenPlant;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IsPlantCommand implements AddonSubCommand {
    @Override
    public boolean execute(BreweryGarden addon, BreweryGardenConfig config, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        GardenRegistry gardenRegistry = BreweryGarden.getGardenRegistry();

        int maxDistance = 30;
        if (args.length > 0) {
            maxDistance = Integer.parseInt(args[0]);
        }

        GardenPlant gardenPlant = gardenRegistry.getByLocation(player.getTargetBlockExact(maxDistance));
        if (gardenPlant != null) {
            MessageUtil.sendMessage(player, "Found a GardenPlant: " + gardenPlant);
        } else {
            MessageUtil.sendMessage(player, "No GardenPlant found.");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(BreweryGarden addon, CommandSender sender, String label, String[] args) {
        return List.of("<distance>");
    }

    @Override
    public String permission() {
        return "garden.command.isplant";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String usage(String label) {
        return "/" + label + "garden isplant <distance?>";
    }
}
