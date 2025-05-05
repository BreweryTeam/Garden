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

public class GrowthStageCommand implements AddonSubCommand {
    @Override
    public boolean execute(BreweryGarden addon, BreweryGardenConfig config, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        int newGrowthStage = Math.min(Integer.parseInt(args[0]), config.getFullyGrown());

        GardenRegistry gardenRegistry = BreweryGarden.getGardenRegistry();
        GardenPlant gardenPlant = gardenRegistry.getByLocation(player.getTargetBlockExact(30));

        if (gardenPlant == null) {
            MessageUtil.sendMessage(player, "No GardenPlant found.");
            return true;
        }

        gardenPlant.setAge(newGrowthStage);
        if (gardenPlant.isFullyGrown()) {
            gardenPlant.place();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(BreweryGarden addon, CommandSender sender, String label, String[] args) {
        return List.of("1", "2", "3", "4");
    }

    @Override
    public String permission() {
        return "garden.command.growthstage";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String usage(String label) {
        return "/" + label + "garden growthstage";
    }
}
