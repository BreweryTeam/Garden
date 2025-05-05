package dev.jsinco.brewery.garden.commands;

import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.commands.subcomands.GiveCommand;
import dev.jsinco.brewery.garden.commands.subcomands.GrowthStageCommand;
import dev.jsinco.brewery.garden.commands.subcomands.IsPlantCommand;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddonCommandManager implements TabExecutor {

    private final BreweryGardenConfig config = BreweryGarden.getInstance().getPluginConfiguration();
    private final Map<String, AddonSubCommand> subCommands = new HashMap<>();

    {
        subCommands.put("give", new GiveCommand());
        subCommands.put("isplant", new IsPlantCommand());
        subCommands.put("growthstage", new GrowthStageCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(sender, "Provide a subcommand.");
            return true;
        }
        if (!sender.hasPermission(permission())) {
            MessageUtil.sendMessage(sender, "You do not have permission to execute this command.");
            return true;
        }

        AddonSubCommand subCommand = subCommands.get(args[1]);
        if (subCommand == null) {
            MessageUtil.sendMessage(sender, "Unknown subcommand.");
            return true;
        }

        if (subCommand.permission() != null && !sender.hasPermission(subCommand.permission())) {
            MessageUtil.sendMessage(sender, "You do not have permission to execute this command.");
            return true;
        } else if (subCommand.playerOnly() && !(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "You must be a player to execute this command.");
            return true;
        }

        String[] subArgs = new String[args.length - 2];
        System.arraycopy(args, 2, subArgs, 0, args.length - 2);
        if (!subCommand.execute(BreweryGarden.getInstance(), config, sender, label, subArgs)) {
            MessageUtil.sendMessage(sender, subCommand.usage(label));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 2) {
            return subCommands.entrySet()
                    .stream().filter(entry -> entry.getValue().permission() == null ||
                            sender.hasPermission(entry.getValue().permission())).map(Map.Entry::getKey).toList();
        }

        AddonSubCommand subCommand = subCommands.get(args[1]);
        if (subCommand == null) return null;
        String[] subArgs = new String[args.length - 2];
        System.arraycopy(args, 2, subArgs, 0, args.length - 2);
        return subCommand.tabComplete(BreweryGarden.getInstance(), sender, label, subArgs);
    }

    public String permission() {
        return "garden.command";
    }
}
