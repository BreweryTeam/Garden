package dev.jsinco.brewery.garden.commands.subcomands;

import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.commands.AddonSubCommand;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand implements AddonSubCommand {
    @Override
    public boolean execute(BreweryGarden addon, BreweryGardenConfig config, CommandSender sender, String label, String[] args) {
        BreweryGarden.getInstance().reload();
        return true;
    }

    @Override
    public List<String> tabComplete(BreweryGarden addon, CommandSender sender, String label, String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "garden.command.reload";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String usage(String label) {
        return "/garden reload - Reload garden";
    }
}
